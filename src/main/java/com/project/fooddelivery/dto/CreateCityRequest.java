package com.project.fooddelivery.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCityRequest(
    @NotBlank(message = "City name is required")
    String name
) {
}
