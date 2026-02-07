package com.neuralvault.api.controller;

import com.neuralvault.api.dto.RecommendationRequest;
import com.neuralvault.api.entity.AiTool;
import com.neuralvault.api.repository.AiToolRepository;
import com.neuralvault.api.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AiToolController {

    private final AiToolRepository aiToolRepository;
    private final RecommendationService recommendationService;

    @GetMapping("/tools")
    public ResponseEntity<List<AiTool>> getAllTools() {
        return ResponseEntity.ok(aiToolRepository.findAll());
    }

    @GetMapping("/tools/{id}")
    public ResponseEntity<AiTool> getToolById(@PathVariable String id) {
        return aiToolRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tools/category/{category}")
    public ResponseEntity<List<AiTool>> getToolsByCategory(@PathVariable String category) {
        try {
            AiTool.Category cat = AiTool.Category.valueOf(category.toUpperCase());
            return ResponseEntity.ok(aiToolRepository.findByCategory(cat));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/recommend")
    public ResponseEntity<AiTool> recommendTool(@Valid @RequestBody RecommendationRequest request) {
        Optional<AiTool> recommendation = recommendationService.recommend(request.query());
        
        return recommendation
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
