package com.neuralvault.api.filter;

import com.neuralvault.api.config.RateLimitConfig;
import com.neuralvault.api.exception.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private final RateLimitConfig rateLimitConfig;

    public RateLimitFilter(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Only apply rate limiting to /api/recommend endpoint
        String path = httpRequest.getRequestURI();
        if (!path.endsWith("/api/recommend")) {
            chain.doFilter(request, response);
            return;
        }
        
        // Get client IP address
        String clientIp = getClientIp(httpRequest);
        
        // Resolve bucket for this IP
        Bucket bucket = rateLimitConfig.resolveBucket(clientIp);
        
        // Try to consume a token
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            // Request is allowed
            httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000; // Convert to seconds
            log.warn("Rate limit exceeded for IP: {}. Retry after {} seconds", clientIp, waitForRefill);
            throw new RateLimitExceededException(
                String.format("Rate limit exceeded. Try again in %d seconds.", waitForRefill)
            );
        }
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
