package com.project.fooddelivery.service;

import com.project.fooddelivery.domain.OrderStatus;

public record OrderStatusChangedEvent(
    Long orderId,
    String customerUsername,
    String restaurantOwnerUsername,
    String deliveryPartnerUsername,
    OrderStatus status
) {
}
