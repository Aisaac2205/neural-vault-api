package com.neuralvault.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RecommendationRequest(
    @NotBlank String query
) {}
