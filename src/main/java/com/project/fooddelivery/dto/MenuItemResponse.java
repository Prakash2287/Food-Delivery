package com.project.fooddelivery.dto;

import java.math.BigDecimal;

public record MenuItemResponse(
    Long id,
    String name,
    BigDecimal price,
    Integer stock,
    boolean available
) {
}
