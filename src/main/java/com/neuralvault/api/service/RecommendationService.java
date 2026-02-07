package com.neuralvault.api.service;

import com.neuralvault.api.entity.AiTool;
import com.neuralvault.api.repository.AiToolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final AiToolRepository aiToolRepository;
    private final GeminiClient geminiClient;

    @Cacheable(value = "recommendations", key = "#query.toLowerCase().trim()")
    public Optional<AiTool> recommend(String query) {
        long startTime = System.currentTimeMillis();

        // Basic validation only - max length
        if (query == null || query.trim().isEmpty()) {
            return Optional.empty();
        }

        String sanitizedQuery = query.trim();

        if (sanitizedQuery.length() > 500) {
            log.warn("Query too long ({} chars), truncating to 500", sanitizedQuery.length());
            sanitizedQuery = sanitizedQuery.substring(0, 500);
        }

        log.info("Cache MISS - Processing recommendation request for query: {}", sanitizedQuery);

        List<AiTool> allTools = aiToolRepository.findAll();

        if (allTools.isEmpty()) {
            return Optional.empty();
        }

        long toolsContextStart = System.currentTimeMillis();
        String toolsContext = allTools.stream()
                .map(tool -> String.format("- %s: %s", tool.getId(), tool.getSpecialty()))
                .collect(Collectors.joining("\n"));

        // Simplified prompt - shorter, faster, cheaper
        String promptText = String.format(
            "Available AI tools:\n%s\n\nUser needs: %s\n\nRespond with ONLY the best tool ID or 'null'. No explanations.",
            toolsContext, sanitizedQuery
        );
        log.debug("Prompt length: {} chars", promptText.length());

        try {
            log.debug("Sending prompt to Gemini API");

            // Llamada a la API de Gemini
            String content = geminiClient.generateContent(promptText);

            if (content == null) {
                log.warn("Received null response from Gemini");
                log.info("Total request time: {}ms", System.currentTimeMillis() - startTime);
                return Optional.empty();
            }

            log.info("Gemini response: {}", content);

            // Validación estricta de la respuesta
            String cleanContent = content.trim();
            
            // Si responde null, no hay recomendación
            if ("null".equalsIgnoreCase(cleanContent)) {
                return Optional.empty();
            }

            // Extraer solo caracteres permitidos para IDs
            String cleanId = cleanContent.replaceAll("[^a-zA-Z0-9_-]", "").trim();
            
            // Validate that the extracted ID is not empty
            if (cleanId.isEmpty()) {
                log.warn("Gemini response does not contain a valid ID: {}", content);
                return Optional.empty();
            }

            // Validate that the ID exists in the list of available tools
            // This prevents Gemini from returning injected or invented IDs
            List<String> validIds = allTools.stream()
                    .map(AiTool::getId)
                    .toList();

            if (!validIds.contains(cleanId)) {
                log.warn("Gemini returned an invalid or non-existent ID: '{}' (Valid IDs: {})",
                        cleanId, validIds);
                return Optional.empty();
            }

            log.info("Valid ID found: {}", cleanId);
            log.info("Total recommendation time: {}ms", System.currentTimeMillis() - startTime);
            return aiToolRepository.findById(cleanId);

        } catch (Exception e) {
            long errorTime = System.currentTimeMillis();
            log.error("Error calling Gemini API after {}ms: {}", errorTime - startTime, e.getMessage(), e);
            return Optional.empty();
        }
    }
}
