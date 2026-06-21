package com.project.fooddelivery.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long menuItemId,
    String menuItemName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal
) {
}
