package com.project.fooddelivery.dto;

import jakarta.validation.constraints.NotNull;

public record OrderDecisionRequest(
    @NotNull(message = "Accepted flag is required")
    Boolean accepted
) {
}
