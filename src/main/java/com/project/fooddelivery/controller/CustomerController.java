package com.project.fooddelivery.controller;

import com.project.fooddelivery.dto.CreateReviewRequest;
import com.project.fooddelivery.dto.MenuItemResponse;
import com.project.fooddelivery.dto.OrderResponse;
import com.project.fooddelivery.dto.PlaceOrderRequest;
import com.project.fooddelivery.dto.RestaurantResponse;
import com.project.fooddelivery.dto.ReviewResponse;
import com.project.fooddelivery.service.CatalogService;
import com.project.fooddelivery.service.OrderService;
import com.project.fooddelivery.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final CatalogService catalogService;
    private final OrderService orderService;
    private final ReviewService reviewService;

    public CustomerController(CatalogService catalogService, OrderService orderService, ReviewService reviewService) {
        this.catalogService = catalogService;
        this.orderService = orderService;
        this.reviewService = reviewService;
    }

    @GetMapping("/restaurants")
    public List<RestaurantResponse> restaurants(@RequestParam String city) {
        return catalogService.findRestaurantsByCity(city);
    }

    @GetMapping("/menu/{restaurantId}")
    public List<MenuItemResponse> menu(@PathVariable Long restaurantId) {
        return catalogService.findMenuByRestaurant(restaurantId);
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return orderService.placeOrder(request);
    }

    @GetMapping("/orders")
    public List<OrderResponse> orders() {
        return orderService.customerOrders();
    }

    @GetMapping("/orders/{orderId}")
    public OrderResponse order(@PathVariable Long orderId) {
        return orderService.customerOrder(orderId);
    }

    @PostMapping("/orders/{orderId}/review")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(@PathVariable Long orderId, @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.createReview(orderId, request);
    }
}
