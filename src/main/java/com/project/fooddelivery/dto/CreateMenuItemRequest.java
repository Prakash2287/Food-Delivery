package com.project.fooddelivery.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateMenuItemRequest(
    @NotBlank(message = "Name is required")
    String name,
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.1", message = "Price must be positive")
    BigDecimal price,
    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    Integer stock
) {
}
