package com.project.fooddelivery.dto;

import com.project.fooddelivery.domain.AssignmentStatus;
import com.project.fooddelivery.domain.OrderStatus;

public record DeliveryAssignmentResponse(
    Long assignmentId,
    Long orderId,
    String restaurantName,
    String cityName,
    AssignmentStatus assignmentStatus,
    OrderStatus orderStatus,
    String partnerUsername
) {
}
