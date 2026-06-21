package com.project.fooddelivery.dto;

import com.project.fooddelivery.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
    @NotNull(message = "Status is required")
    OrderStatus status
) {
}
