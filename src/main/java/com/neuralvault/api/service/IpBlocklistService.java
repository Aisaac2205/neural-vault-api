package com.neuralvault.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class IpBlocklistService {

    // IPs temporarily blocked due to suspicious activity
    private final ConcurrentHashMap<String, BlockedIp> blockedIps = new ConcurrentHashMap<>();

    // IPs with suspicious behavior score
    private final ConcurrentHashMap<String, Integer> suspicionScores = new ConcurrentHashMap<>();

    private static final int BLOCK_DURATION_MINUTES = 60;
    private static final int SUSPICION_THRESHOLD = 5;
    private static final int MAX_Suspicion_SCORE = 10;

    public record BlockedIp(String ip, Instant blockedAt, String reason) {}

    public boolean isBlocked(String ip) {
        BlockedIp blocked = blockedIps.get(ip);
        if (blocked == null) {
            return false;
        }

        // Check if block has expired
        Instant expiryTime = blocked.blockedAt().plusSeconds(BLOCK_DURATION_MINUTES * 60);
        if (Instant.now().isAfter(expiryTime)) {
            blockedIps.remove(ip);
            suspicionScores.remove(ip);
            log.info("IP {} has been unblocked after {} minutes", ip, BLOCK_DURATION_MINUTES);
            return false;
        }

        return true;
    }

    public void blockIp(String ip, String reason) {
        blockedIps.put(ip, new BlockedIp(ip, Instant.now(), reason));
        log.warn("IP {} has been blocked for {} minutes. Reason: {}", ip, BLOCK_DURATION_MINUTES, reason);
    }

    public void addSuspicionScore(String ip, int points) {
        int newScore = suspicionScores.merge(ip, points, Integer::sum);
        
        if (newScore >= SUSPICION_THRESHOLD) {
            blockIp(ip, "Accumulated suspicion score: " + newScore);
        } else if (newScore > 0) {
            log.debug("IP {} suspicion score increased to {}", ip, newScore);
        }

        // Cap the score
        if (newScore > MAX_Suspicion_SCORE) {
            suspicionScores.put(ip, MAX_Suspicion_SCORE);
        }
    }

    public void decreaseSuspicionScore(String ip) {
        suspicionScores.computeIfPresent(ip, (k, v) -> {
            int newValue = v - 1;
            return newValue > 0 ? newValue : null;
        });
    }

    public Set<String> getBlockedIps() {
        return blockedIps.keySet();
    }

    public int getSuspicionScore(String ip) {
        return suspicionScores.getOrDefault(ip, 0);
    }

    public void cleanupExpiredBlocks() {
        Instant now = Instant.now();
        blockedIps.entrySet().removeIf(entry -> {
            Instant expiryTime = entry.getValue().blockedAt().plusSeconds(BLOCK_DURATION_MINUTES * 60);
            boolean expired = now.isAfter(expiryTime);
            if (expired) {
                suspicionScores.remove(entry.getKey());
            }
            return expired;
        });
    }
}
