package com.project.fooddelivery.dto;

public record RestaurantResponse(
    Long id,
    String name,
    String ownerUsername,
    String cityName
) {
}
