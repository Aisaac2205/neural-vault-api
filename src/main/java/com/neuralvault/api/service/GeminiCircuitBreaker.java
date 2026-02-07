package com.neuralvault.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class GeminiCircuitBreaker {

    // Circuit breaker states
    public enum State {
        CLOSED,     // Normal operation
        OPEN,       // Failing, rejecting requests
        HALF_OPEN   // Testing if service recovered
    }

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private volatile Instant lastFailureTime = null;

    // Configuration
    private static final int FAILURE_THRESHOLD = 5;        // Open circuit after 5 failures
    private static final int SUCCESS_THRESHOLD = 3;        // Close circuit after 3 successes in half-open
    private static final long TIMEOUT_SECONDS = 60;        // Try half-open after 60 seconds
    private static final long DAILY_REQUEST_LIMIT = 1000;  // Max requests per day

    // Daily counter
    private volatile Instant dayStart = Instant.now();
    private final AtomicInteger dailyRequestCount = new AtomicInteger(0);

    public boolean allowRequest() {
        // Check daily limit first
        if (!checkDailyLimit()) {
            log.warn("Daily request limit reached. Blocking request to protect API quota.");
            return false;
        }

        State currentState = state.get();
        
        switch (currentState) {
            case CLOSED:
                return true;
                
            case OPEN:
                if (shouldAttemptReset()) {
                    state.set(State.HALF_OPEN);
                    successCount.set(0);
                    log.info("Circuit breaker entering HALF_OPEN state - testing Gemini API");
                    return true;
                }
                log.debug("Circuit breaker is OPEN - blocking request to Gemini API");
                return false;
                
            case HALF_OPEN:
                return true;
                
            default:
                return false;
        }
    }

    public void recordSuccess() {
        dailyRequestCount.incrementAndGet();
        
        if (state.get() == State.HALF_OPEN) {
            int successes = successCount.incrementAndGet();
            if (successes >= SUCCESS_THRESHOLD) {
                state.set(State.CLOSED);
                failureCount.set(0);
                log.info("Circuit breaker CLOSED - Gemini API is healthy");
            }
        } else {
            failureCount.set(0); // Reset failure count on success
        }
    }

    public void recordFailure() {
        dailyRequestCount.incrementAndGet();
        lastFailureTime = Instant.now();
        
        int failures = failureCount.incrementAndGet();
        
        if (state.get() == State.HALF_OPEN) {
            state.set(State.OPEN);
            log.warn("Circuit breaker OPEN - Gemini API failed in half-open state");
        } else if (failures >= FAILURE_THRESHOLD) {
            state.set(State.OPEN);
            log.warn("Circuit breaker OPEN - Gemini API failure threshold reached ({}/{})", 
                    failures, FAILURE_THRESHOLD);
        }
    }

    public State getState() {
        return state.get();
    }

    public int getDailyRequestCount() {
        return dailyRequestCount.get();
    }

    public int getRemainingDailyRequests() {
        return (int) (DAILY_REQUEST_LIMIT - dailyRequestCount.get());
    }

    public void resetDailyCounter() {
        Instant now = Instant.now();
        if (now.minusSeconds(86400).isAfter(dayStart)) {
            dayStart = now;
            dailyRequestCount.set(0);
            log.info("Daily request counter reset");
        }
    }

    private boolean checkDailyLimit() {
        resetDailyCounter();
        return dailyRequestCount.get() < DAILY_REQUEST_LIMIT;
    }

    private boolean shouldAttemptReset() {
        if (lastFailureTime == null) {
            return true;
        }
        return Instant.now().minusSeconds(TIMEOUT_SECONDS).isAfter(lastFailureTime);
    }

    public void forceOpen(String reason) {
        state.set(State.OPEN);
        lastFailureTime = Instant.now();
        log.warn("Circuit breaker forcibly OPENED. Reason: {}", reason);
    }

    public void forceClose() {
        state.set(State.CLOSED);
        failureCount.set(0);
        log.info("Circuit breaker forcibly CLOSED");
    }
}
