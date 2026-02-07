package com.neuralvault.api.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuralvault.api.config.RateLimitConfig;
import com.neuralvault.api.dto.RecommendationRequest;
import com.neuralvault.api.exception.RateLimitExceededException;
import com.neuralvault.api.service.BotDetectionService;
import com.neuralvault.api.service.IpBlocklistService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private final RateLimitConfig rateLimitConfig;
    private final IpBlocklistService ipBlocklistService;
    private final BotDetectionService botDetectionService;
    private final ObjectMapper objectMapper;

    // Patterns to detect injection attempts
    private static final Pattern[] SUSPICIOUS_PATTERNS = {
        Pattern.compile("(?i)ignore.*previous"),
        Pattern.compile("(?i)disregard.*above"),
        Pattern.compile("(?i)forget.*everything"),
        Pattern.compile("(?i)you are now"),
        Pattern.compile("(?i)system\\s*:"),
        Pattern.compile("(?i)developer\\s*:"),
        Pattern.compile("<\\s*script", Pattern.CASE_INSENSITIVE),
    };

    public RateLimitFilter(RateLimitConfig rateLimitConfig,
                          IpBlocklistService ipBlocklistService,
                          BotDetectionService botDetectionService) {
        this.rateLimitConfig = rateLimitConfig;
        this.ipBlocklistService = ipBlocklistService;
        this.botDetectionService = botDetectionService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Only apply to /api/recommend endpoint
        String path = httpRequest.getRequestURI();
        if (!path.endsWith("/api/recommend")) {
            chain.doFilter(request, response);
            return;
        }

        // Get client IP
        String clientIp = getClientIp(httpRequest);

        // 1. Check if IP is blocked
        if (ipBlocklistService.isBlocked(clientIp)) {
            log.warn("Blocked IP attempted request: {}", clientIp);
            httpResponse.setStatus(403);
            httpResponse.getWriter().write("Access denied");
            return;
        }

        // 2. Bot detection and fingerprinting
        BotDetectionService.RequestFingerprint fingerprint = botDetectionService.analyzeRequest(httpRequest);

        // Block high-risk bots immediately
        if (botDetectionService.shouldBlock(fingerprint)) {
            ipBlocklistService.blockIp(clientIp, "High-risk bot detected: " + fingerprint.userAgent());
            log.warn("High-risk bot blocked - IP: {}, Score: {}", clientIp, fingerprint.suspicionScore());
            httpResponse.setStatus(403);
            httpResponse.getWriter().write("Access denied");
            return;
        }

        // 3. Check for suspicious content in request
        boolean isContentSuspicious = isSuspiciousRequest(httpRequest);

        // 4. Determine if this is a suspicious request (bot OR content)
        boolean isSuspicious = botDetectionService.isLikelyBot(fingerprint) || isContentSuspicious;

        // Add suspicion score for bots
        if (botDetectionService.isLikelyBot(fingerprint)) {
            ipBlocklistService.addSuspicionScore(clientIp, fingerprint.suspicionScore());
        }

        // 5. Apply rate limiting
        Bucket bucket;
        if (isSuspicious) {
            bucket = rateLimitConfig.resolveSuspiciousBucket(clientIp);
        } else {
            bucket = rateLimitConfig.resolveBucket(clientIp);
        }

        // Suspicious requests consume more tokens
        int tokensToConsume = isSuspicious ? 3 : 1;

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(tokensToConsume);

        if (probe.isConsumed()) {
            // Request allowed
            httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));

            if (isSuspicious) {
                httpResponse.setHeader("X-RateLimit-Suspicious", "true");
                log.warn("Suspicious request allowed but marked - IP: {}, Bot: {}, Content: {}",
                        clientIp, botDetectionService.isLikelyBot(fingerprint), isContentSuspicious);
            }

            // Decrease suspicion score for good behavior
            if (!isSuspicious) {
                ipBlocklistService.decreaseSuspicionScore(clientIp);
            }

            chain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;

            // Add suspicion for rate limit abuse
            ipBlocklistService.addSuspicionScore(clientIp, 2);

            log.warn("Rate limit exceeded for IP: {}. Retry after {} seconds. Suspicious: {}",
                    clientIp, waitForRefill, isSuspicious);

            throw new RateLimitExceededException(
                String.format("Rate limit exceeded. Try again in %d seconds.", waitForRefill)
            );
        }
    }

    private boolean isSuspiciousRequest(HttpServletRequest request) {
        try {
            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                return false;
            }

            // Read request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            String body = sb.toString();

            if (body.isEmpty()) {
                return false;
            }

            try {
                RecommendationRequest recRequest = objectMapper.readValue(body, RecommendationRequest.class);
                if (recRequest.query() != null) {
                    String query = recRequest.query().toLowerCase();

                    // Check for suspicious patterns
                    for (Pattern pattern : SUSPICIOUS_PATTERNS) {
                        if (pattern.matcher(query).find()) {
                            log.warn("Suspicious pattern detected in query: {}", recRequest.query());
                            return true;
                        }
                    }

                    // Check for dangerous characters
                    if (query.contains("<") || query.contains(">") || query.contains("\"") ||
                        query.contains("'") || query.contains(";")) {
                        log.warn("Dangerous characters detected in query");
                        return true;
                    }
                }
            } catch (Exception e) {
                log.warn("Could not parse request body as RecommendationRequest");
                return true;
            }

        } catch (Exception e) {
            log.error("Error checking suspicious request", e);
        }

        return false;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
