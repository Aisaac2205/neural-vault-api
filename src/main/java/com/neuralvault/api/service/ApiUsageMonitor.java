package com.neuralvault.api.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class ApiUsageMonitor {

    private final GeminiCircuitBreaker circuitBreaker;
    private final IpBlocklistService ipBlocklistService;

    // Statistics
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger blockedRequests = new AtomicInteger(0);
    private final AtomicInteger suspiciousRequests = new AtomicInteger(0);
    private final AtomicLong lastResetTime = new AtomicLong(System.currentTimeMillis());

    // Thresholds for alerts
    private static final int ALERT_THRESHOLD_REQUESTS = 100;  // Alert if > 100 req/hour
    private static final int ALERT_THRESHOLD_BLOCKED = 20;    // Alert if > 20 blocked/hour
    private static final int ALERT_THRESHOLD_DAILY = 800;     // Alert when approaching daily limit

    private static final DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public ApiUsageMonitor(GeminiCircuitBreaker circuitBreaker,
                          IpBlocklistService ipBlocklistService) {
        this.circuitBreaker = circuitBreaker;
        this.ipBlocklistService = ipBlocklistService;
    }

    @PostConstruct
    public void init() {
        log.info("API Usage Monitor initialized");
    }

    public void recordRequest() {
        totalRequests.incrementAndGet();
    }

    public void recordBlockedRequest() {
        blockedRequests.incrementAndGet();
    }

    public void recordSuspiciousRequest() {
        suspiciousRequests.incrementAndGet();
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    public void generateHourlyReport() {
        int total = totalRequests.get();
        int blocked = blockedRequests.get();
        int suspicious = suspiciousRequests.get();
        int dailyRemaining = circuitBreaker.getRemainingDailyRequests();
        int blockedIps = ipBlocklistService.getBlockedIps().size();

        log.info("=== API Usage Report (Last Hour) ===");
        log.info("Total requests: {}", total);
        log.info("Blocked requests: {} ({}%)", blocked, total > 0 ? (blocked * 100 / total) : 0);
        log.info("Suspicious requests: {} ({}%)", suspicious, total > 0 ? (suspicious * 100 / total) : 0);
        log.info("Daily API quota remaining: {}/1000", dailyRemaining);
        log.info("Currently blocked IPs: {}", blockedIps);
        log.info("Circuit breaker state: {}", circuitBreaker.getState());

        // Check thresholds and alert
        if (total > ALERT_THRESHOLD_REQUESTS) {
            log.warn("ALERT: High request volume detected: {} requests/hour", total);
        }

        if (blocked > ALERT_THRESHOLD_BLOCKED) {
            log.warn("ALERT: High number of blocked requests: {} blocks/hour", blocked);
        }

        if (dailyRemaining < (1000 - ALERT_THRESHOLD_DAILY)) {
            log.warn("ALERT: Approaching daily API limit. Remaining: {}", dailyRemaining);
        }

        // Reset hourly counters
        totalRequests.set(0);
        blockedRequests.set(0);
        suspiciousRequests.set(0);
        lastResetTime.set(System.currentTimeMillis());

        // Cleanup expired IP blocks
        ipBlocklistService.cleanupExpiredBlocks();
    }

    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    public void generateDailyReport() {
        log.info("=== Daily API Usage Report ===");
        log.info("Daily API requests used: {}/1000", circuitBreaker.getDailyRequestCount());
        log.info("Circuit breaker state at EOD: {}", circuitBreaker.getState());
        log.info("IPs currently blocked: {}", ipBlocklistService.getBlockedIps().size());
    }

    public String getStatus() {
        return String.format(
            "API Status: %d requests/hour, %d blocked, %d suspicious, %d daily remaining, Circuit: %s",
            totalRequests.get(),
            blockedRequests.get(),
            suspiciousRequests.get(),
            circuitBreaker.getRemainingDailyRequests(),
            circuitBreaker.getState()
        );
    }
}
