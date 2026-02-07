package com.neuralvault.api.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GeminiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-2.5-flash-lite}")
    private String model;

    private final GeminiCircuitBreaker circuitBreaker;
    private Client client;

    public GeminiClient(GeminiCircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    @PostConstruct
    public void validateConfiguration() {
        if (apiKey == null || apiKey.isEmpty()) {
            log.error("GEMINI_API_KEY is not configured");
        } else if (apiKey.length() < 20) {
            log.warn("GEMINI_API_KEY appears to be invalid (too short)");
        } else {
            log.info("Gemini API configured. Model: {}", model);
        }
    }

    private synchronized void initializeClient() {
        if (client == null && apiKey != null && !apiKey.isEmpty()) {
            try {
                client = Client.builder()
                        .apiKey(apiKey)
                        .build();
            } catch (Exception e) {
                log.error("Failed to initialize Gemini client: {}", e.getMessage());
            }
        }
    }

    public String generateContent(String prompt) {
        if (!circuitBreaker.allowRequest()) {
            log.warn("Circuit breaker OPEN. Daily remaining: {}", circuitBreaker.getRemainingDailyRequests());
            return null;
        }

        try {
            if (client == null) {
                initializeClient();
            }

            if (client == null) {
                log.error("Gemini client not initialized");
                circuitBreaker.recordFailure();
                return null;
            }

            GenerateContentResponse response = client.models.generateContent(model, prompt, null);

            if (response != null && response.text() != null) {
                String text = response.text();
                circuitBreaker.recordSuccess();
                return text;
            } else {
                log.warn("Empty response from Gemini API");
                circuitBreaker.recordFailure();
                return null;
            }

        } catch (Exception e) {
            log.error("Gemini API error: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            circuitBreaker.recordFailure();
            return null;
        }
    }

    public GeminiCircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}
