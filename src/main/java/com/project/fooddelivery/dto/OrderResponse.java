package com.project.fooddelivery.dto;

import com.project.fooddelivery.domain.OrderStatus;
import com.project.fooddelivery.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long id,
    String customerUsername,
    Long restaurantId,
    String restaurantName,
    OrderStatus status,
    PaymentStatus paymentStatus,
    BigDecimal totalAmount,
    LocalDateTime createdAt,
    List<OrderItemResponse> items
) {
}
