package com.project.fooddelivery.controller;

import com.project.fooddelivery.domain.OrderStatus;
import com.project.fooddelivery.dto.CreateMenuItemRequest;
import com.project.fooddelivery.dto.MenuItemResponse;
import com.project.fooddelivery.dto.OrderDecisionRequest;
import com.project.fooddelivery.dto.OrderResponse;
import com.project.fooddelivery.dto.RestaurantResponse;
import com.project.fooddelivery.dto.UpdateMenuItemStockRequest;
import com.project.fooddelivery.dto.UpdateOrderStatusRequest;
import com.project.fooddelivery.service.OwnerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @GetMapping("/restaurants")
    public List<RestaurantResponse> myRestaurants() {
        return ownerService.ownedRestaurants();
    }

    @GetMapping("/orders")
    public List<OrderResponse> myOrders() {
        return ownerService.ownedOrders();
    }

    @PostMapping("/restaurants/{restaurantId}/menu-items")
    public MenuItemResponse createMenuItem(@PathVariable Long restaurantId, @Valid @RequestBody CreateMenuItemRequest request) {
        return ownerService.createMenuItem(restaurantId, request);
    }

    @PatchMapping("/restaurants/{restaurantId}/menu-items/{menuItemId}")
    public MenuItemResponse updateStock(
        @PathVariable Long restaurantId,
        @PathVariable Long menuItemId,
        @Valid @RequestBody UpdateMenuItemStockRequest request
    ) {
        return ownerService.updateStock(restaurantId, menuItemId, request);
    }

    @PostMapping("/orders/{orderId}/decision")
    public OrderResponse decideOrder(@PathVariable Long orderId, @Valid @RequestBody OrderDecisionRequest request) {
        return ownerService.decideOrder(orderId, request.accepted());
    }

    @PatchMapping("/orders/{orderId}/status")
    public OrderResponse updateOrderStatus(@PathVariable Long orderId, @Valid @RequestBody UpdateOrderStatusRequest request) {
        if (request.status() != OrderStatus.PREPARING) {
            throw new IllegalStateException("Owner endpoint only accepts PREPARING");
        }
        return ownerService.updateOrderStatus(orderId, request.status());
    }
}
