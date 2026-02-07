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
            log.error("GEMINI_API_KEY is not configured. Application will run but recommendations will not be available.");
        } else if (apiKey.length() < 20) {
            log.warn("GEMINI_API_KEY appears to be invalid (too short). Please check your configuration.");
        } else {
            log.info("Gemini API key configured correctly");
        }
        
        log.info("Using Gemini model: {}", model);
    }

    private synchronized void initializeClient() {
        if (client == null && apiKey != null && !apiKey.isEmpty()) {
            try {
                client = Client.builder()
                        .apiKey(apiKey)
                        .build();
                log.info("Google GenAI client initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Google GenAI client: {}", e.getMessage(), e);
            }
        }
    }

    public String generateContent(String prompt) {
        long startTime = System.currentTimeMillis();
        log.info("Starting Gemini API request");

        // Check circuit breaker first
        if (!circuitBreaker.allowRequest()) {
            log.warn("Request blocked by circuit breaker. State: {}, Daily remaining: {}",
                    circuitBreaker.getState(), circuitBreaker.getRemainingDailyRequests());
            return null;
        }

        try {
            long checkTime = System.currentTimeMillis();
            log.debug("Circuit breaker check took: {}ms", checkTime - startTime);

            if (client == null) {
                initializeClient();
            }

            if (client == null) {
                log.error("Google GenAI client is not initialized. Check your API key.");
                circuitBreaker.recordFailure();
                return null;
            }

            log.info("Sending request to Gemini model: {} (Daily: {}/1000)",
                    model, circuitBreaker.getDailyRequestCount());
            log.debug("Prompt length: {} characters", prompt.length());

            long beforeSendTime = System.currentTimeMillis();
            log.debug("Client init took: {}ms", beforeSendTime - checkTime);

            GenerateContentResponse response = client.models.generateContent(model, prompt, null);

            long responseTime = System.currentTimeMillis();
            log.info("Gemini API response received in: {}ms", responseTime - beforeSendTime);

            if (response != null && response.text() != null) {
                String text = response.text();
                log.debug("Generated text: {}", text);
                log.info("Total request time: {}ms", System.currentTimeMillis() - startTime);
                circuitBreaker.recordSuccess();
                return text;
            } else {
                log.warn("Received empty response from Gemini API");
                circuitBreaker.recordFailure();
                return null;
            }

        } catch (Exception e) {
            long errorTime = System.currentTimeMillis();
            long duration = errorTime - startTime;
            log.error("Error generating content with Gemini API after {}ms: {} - {}",
                    duration, e.getClass().getSimpleName(), e.getMessage());
            circuitBreaker.recordFailure();
            return null;
        }
    }

    public GeminiCircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}
