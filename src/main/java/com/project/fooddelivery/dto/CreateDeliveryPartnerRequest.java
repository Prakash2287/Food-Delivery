package com.project.fooddelivery.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDeliveryPartnerRequest(
    @NotBlank(message = "Username is required")
    String username,
    @NotBlank(message = "Full name is required")
    String fullName,
    @NotBlank(message = "City name is required")
    String cityName
) {
}
