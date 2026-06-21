package com.project.fooddelivery.service;

import com.project.fooddelivery.domain.City;
import com.project.fooddelivery.domain.CustomerOrder;
import com.project.fooddelivery.domain.DeliveryAssignment;
import com.project.fooddelivery.domain.MenuItem;
import com.project.fooddelivery.domain.OrderItem;
import com.project.fooddelivery.domain.Restaurant;
import com.project.fooddelivery.domain.Review;
import com.project.fooddelivery.dto.CityResponse;
import com.project.fooddelivery.dto.DeliveryAssignmentResponse;
import com.project.fooddelivery.dto.MenuItemResponse;
import com.project.fooddelivery.dto.OrderItemResponse;
import com.project.fooddelivery.dto.OrderResponse;
import com.project.fooddelivery.dto.RestaurantResponse;
import com.project.fooddelivery.dto.ReviewResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ApiMapper {

    public CityResponse toCityResponse(City city) {
        return new CityResponse(city.getId(), city.getName());
    }

    public RestaurantResponse toRestaurantResponse(Restaurant restaurant) {
        return new RestaurantResponse(
            restaurant.getId(),
            restaurant.getName(),
            restaurant.getOwnerUsername(),
            restaurant.getCity().getName()
        );
    }

    public MenuItemResponse toMenuItemResponse(MenuItem menuItem) {
        return new MenuItemResponse(
            menuItem.getId(),
            menuItem.getName(),
            menuItem.getPrice(),
            menuItem.getStock(),
            menuItem.isAvailable()
        );
    }

    public OrderResponse toOrderResponse(CustomerOrder order) {
        List<OrderItemResponse> items = order.getItems().stream()
            .map(this::toOrderItemResponse)
            .toList();

        return new OrderResponse(
            order.getId(),
            order.getCustomerUsername(),
            order.getRestaurant().getId(),
            order.getRestaurant().getName(),
            order.getStatus(),
            order.getPaymentStatus(),
            order.getTotalAmount(),
            order.getCreatedAt(),
            items
        );
    }

    public DeliveryAssignmentResponse toAssignmentResponse(DeliveryAssignment assignment) {
        return new DeliveryAssignmentResponse(
            assignment.getId(),
            assignment.getOrder().getId(),
            assignment.getOrder().getRestaurant().getName(),
            assignment.getOrder().getRestaurant().getCity().getName(),
            assignment.getStatus(),
            assignment.getOrder().getStatus(),
            assignment.getPartner() == null ? null : assignment.getPartner().getUsername()
        );
    }

    public ReviewResponse toReviewResponse(Review review) {
        return new ReviewResponse(
            review.getId(),
            review.getOrder().getId(),
            review.getCustomerUsername(),
            review.getRating(),
            review.getComment()
        );
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return new OrderItemResponse(
            item.getMenuItemId(),
            item.getMenuItemName(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getLineTotal()
        );
    }
}
