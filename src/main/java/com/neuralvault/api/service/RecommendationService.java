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
        log.info("Cache MISS - Processing recommendation request for query: {}", query);

        List<AiTool> allTools = aiToolRepository.findAll();
        // Si no hay herramientas, salimos rápido
        if (allTools.isEmpty()) {
            return Optional.empty();
        }

        String toolsContext = allTools.stream()
                .map(tool -> String.format("- %s: %s", tool.getId(), tool.getSpecialty()))
                .collect(Collectors.joining("\n"));

        String promptText = String.format("""
                Actúa como un experto en software e inteligencia artificial.
                El usuario necesita: '%s'
                Basado en esta lista de herramientas disponibles:
                %s
                Responde ÚNICAMENTE con el ID exacto de la mejor herramienta para el trabajo.
                Si ninguna herramienta sirve para esta necesidad específica, responde exactamente 'null'.
                No incluyas explicaciones ni texto adicional, solo el ID o 'null'.
                """, query, toolsContext);

        try {
            log.debug("Sending prompt to Gemini API");

            // Llamada simple a la API de Gemini
            String content = geminiClient.generateContent(promptText);
            
            if (content == null) {
                log.warn("Received null response from Gemini");
                return Optional.empty();
            }
            
            log.info("Gemini response: {}", content);

            String cleanContent = content.trim();
            if ("null".equalsIgnoreCase(cleanContent)) {
                return Optional.empty();
            }

            String cleanId = cleanContent.replaceAll("[^a-zA-Z0-9_-]", "").trim();
            return aiToolRepository.findById(cleanId);

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
