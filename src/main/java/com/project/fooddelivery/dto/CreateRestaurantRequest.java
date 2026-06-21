package com.project.fooddelivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRestaurantRequest(
    @NotBlank(message = "Restaurant name is required")
    String name,
    @NotBlank(message = "Owner username is required")
    String ownerUsername,
    @NotNull(message = "City id is required")
    Long cityId
) {
}
