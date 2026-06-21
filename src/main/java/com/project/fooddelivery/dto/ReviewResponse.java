package com.project.fooddelivery.dto;

public record ReviewResponse(
    Long id,
    Long orderId,
    String customerUsername,
    Integer rating,
    String comment
) {
}
