package com.neuralvault.api.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Component
public class BotDetectionService {

    // Known bot user agents
    private static final Set<String> KNOWN_BOTS = Set.of(
        "bot", "crawler", "spider", "scraper", "curl", "wget", "python-requests",
        "httpclient", "axios", "postman", "insomnia", "scrapy", "selenium",
        "playwright", "puppeteer", "headless", "phantomjs", "slimerjs"
    );

    // Suspicious patterns in headers
    private static final Pattern[] SUSPICIOUS_PATTERNS = {
        Pattern.compile("(?i)burp|sqlmap|nikto|nmap|masscan|zgrab"),
        Pattern.compile("(?i)nessus|openvas|qualys|acunetix|netsparker"),
    };

    // Legitimate browser patterns
    private static final Pattern LEGITIMATE_BROWSER = Pattern.compile(
        "(?i)(mozilla|chrome|safari|firefox|edge|opera|trident)"
    );

    public record RequestFingerprint(
        String userAgent,
        String acceptHeader,
        String acceptLanguage,
        String acceptEncoding,
        boolean hasReferer,
        boolean hasValidHeaders,
        boolean isKnownBot,
        boolean isLegitimateBrowser,
        int suspicionScore
    ) {}

    public RequestFingerprint analyzeRequest(HttpServletRequest request) {
        Map<String, String> headers = extractHeaders(request);
        
        String userAgent = headers.getOrDefault("user-agent", "").toLowerCase();
        String acceptHeader = headers.getOrDefault("accept", "");
        String acceptLanguage = headers.getOrDefault("accept-language", "");
        String acceptEncoding = headers.getOrDefault("accept-encoding", "");
        String referer = headers.getOrDefault("referer", "");

        boolean hasReferer = !referer.isEmpty();
        boolean isKnownBot = detectKnownBot(userAgent, headers);
        boolean isLegitimateBrowser = LEGITIMATE_BROWSER.matcher(userAgent).find();
        boolean hasValidHeaders = validateHeaders(headers);
        int suspicionScore = calculateSuspicionScore(userAgent, headers, hasValidHeaders);

        return new RequestFingerprint(
            userAgent,
            acceptHeader,
            acceptLanguage,
            acceptEncoding,
            hasReferer,
            hasValidHeaders,
            isKnownBot,
            isLegitimateBrowser,
            suspicionScore
        );
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name.toLowerCase(), request.getHeader(name));
        }
        
        return headers;
    }

    private boolean detectKnownBot(String userAgent, Map<String, String> headers) {
        if (userAgent == null || userAgent.isEmpty()) {
            return true; // Empty user agent is suspicious
        }

        for (String bot : KNOWN_BOTS) {
            if (userAgent.contains(bot)) {
                return true;
            }
        }

        // Check headers commonly missing in bots
        if (!headers.containsKey("accept-language") || 
            !headers.containsKey("accept-encoding")) {
            return true;
        }

        return false;
    }

    private boolean validateHeaders(Map<String, String> headers) {
        // Legitimate browsers typically send these headers
        if (!headers.containsKey("accept")) {
            return false;
        }

        String accept = headers.get("accept");
        if (!accept.contains("text/html") && !accept.contains("application/json")) {
            // Suspicious: not accepting standard content types
            return false;
        }

        return true;
    }

    private int calculateSuspicionScore(String userAgent, Map<String, String> headers, boolean hasValidHeaders) {
        int score = 0;

        // No user agent
        if (userAgent == null || userAgent.isEmpty()) {
            score += 3;
        }

        // Known bot
        if (detectKnownBot(userAgent, headers)) {
            score += 2;
        }

        // Invalid headers
        if (!hasValidHeaders) {
            score += 2;
        }

        // Suspicious patterns in headers
        for (Pattern pattern : SUSPICIOUS_PATTERNS) {
            for (String headerValue : headers.values()) {
                if (pattern.matcher(headerValue).find()) {
                    score += 5; // High score for security scanner
                    break;
                }
            }
        }

        // Missing common browser headers
        if (!headers.containsKey("accept-language")) {
            score += 1;
        }

        return score;
    }

    public boolean isLikelyBot(RequestFingerprint fingerprint) {
        return fingerprint.isKnownBot() || 
               fingerprint.suspicionScore() >= 3 ||
               !fingerprint.isLegitimateBrowser();
    }

    public boolean shouldBlock(RequestFingerprint fingerprint) {
        return fingerprint.suspicionScore() >= 7 ||
               (fingerprint.isKnownBot() && fingerprint.suspicionScore() >= 5);
    }
}
