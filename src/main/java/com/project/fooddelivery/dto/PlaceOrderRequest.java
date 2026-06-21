package com.project.fooddelivery.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PlaceOrderRequest(
    @NotNull(message = "Restaurant id is required")
    Long restaurantId,
    @NotEmpty(message = "At least one item is required")
    List<@Valid OrderLineRequest> items
) {
}
