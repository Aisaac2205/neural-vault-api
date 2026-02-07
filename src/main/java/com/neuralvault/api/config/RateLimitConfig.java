package com.neuralvault.api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    // Configuration for normal users
    @Value("${ratelimit.capacity:10}")
    private int capacity;

    @Value("${ratelimit.refill.tokens:10}")
    private int refillTokens;

    @Value("${ratelimit.refill.duration:1}")
    private int refillDurationMinutes;

    // Configuration for suspicious users (more restrictive)
    @Value("${ratelimit.suspicious.capacity:3}")
    private int suspiciousCapacity;

    @Value("${ratelimit.suspicious.refill.tokens:3}")
    private int suspiciousRefillTokens;

    // Global limit for Gemini API protection
    @Value("${ratelimit.global.daily.max:1000}")
    private int globalDailyMax;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> suspiciousBuckets = new ConcurrentHashMap<>();

    @Bean
    public ConcurrentHashMap<String, Bucket> buckets() {
        return buckets;
    }

    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k -> createNewBucket());
    }

    public Bucket resolveSuspiciousBucket(String key) {
        return suspiciousBuckets.computeIfAbsent(key, k -> createSuspiciousBucket());
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(capacity, 
            Refill.intervally(refillTokens, Duration.ofMinutes(refillDurationMinutes)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createSuspiciousBucket() {
        Bandwidth limit = Bandwidth.classic(suspiciousCapacity, 
            Refill.intervally(suspiciousRefillTokens, Duration.ofMinutes(refillDurationMinutes)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public int getGlobalDailyMax() {
        return globalDailyMax;
    }
}
