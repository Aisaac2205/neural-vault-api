package com.neuralvault.api.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
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

    private Client client;

    public GeminiClient() {
        // El cliente se inicializará en el primer uso después de que Spring inyecte la apiKey
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
        try {
            if (client == null) {
                initializeClient();
            }

            if (client == null) {
                log.error("Google GenAI client is not initialized. Check your API key.");
                return null;
            }

            log.info("Generating content with model: {}", model);
            
            GenerateContentResponse response = client.models.generateContent(model, prompt, null);
            
            if (response != null && response.text() != null) {
                String text = response.text();
                log.debug("Generated text: {}", text);
                return text;
            } else {
                log.warn("Received empty response from Gemini API");
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error generating content with Gemini API: {}", e.getMessage(), e);
            return null;
        }
    }
}
