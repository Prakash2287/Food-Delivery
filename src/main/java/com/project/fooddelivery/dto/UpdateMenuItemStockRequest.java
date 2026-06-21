package com.project.fooddelivery.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateMenuItemStockRequest(
    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    Integer stock,
    @NotNull(message = "Available flag is required")
    Boolean available
) {
}
