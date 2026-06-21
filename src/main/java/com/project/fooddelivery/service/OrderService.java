package com.project.fooddelivery.service;

import com.project.fooddelivery.domain.CustomerOrder;
import com.project.fooddelivery.domain.MenuItem;
import com.project.fooddelivery.domain.OrderItem;
import com.project.fooddelivery.domain.OrderStatus;
import com.project.fooddelivery.domain.PaymentStatus;
import com.project.fooddelivery.domain.Restaurant;
import com.project.fooddelivery.dto.OrderLineRequest;
import com.project.fooddelivery.dto.OrderResponse;
import com.project.fooddelivery.dto.PlaceOrderRequest;
import com.project.fooddelivery.exception.BusinessException;
import com.project.fooddelivery.exception.ForbiddenException;
import com.project.fooddelivery.exception.NotFoundException;
import com.project.fooddelivery.repository.CustomerOrderRepository;
import com.project.fooddelivery.repository.MenuItemRepository;
import com.project.fooddelivery.repository.RestaurantRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final CurrentUserService currentUserService;
    private final ApiMapper apiMapper;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(
        RestaurantRepository restaurantRepository,
        MenuItemRepository menuItemRepository,
        CustomerOrderRepository customerOrderRepository,
        CurrentUserService currentUserService,
        ApiMapper apiMapper,
        ApplicationEventPublisher eventPublisher
    ) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.currentUserService = currentUserService;
        this.apiMapper = apiMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
            .orElseThrow(() -> new NotFoundException("Restaurant not found"));

        CustomerOrder order = new CustomerOrder(
            currentUserService.username(),
            OrderStatus.PLACED,
            PaymentStatus.PAID,
            restaurant
        );

        BigDecimal total = BigDecimal.ZERO;
        for (OrderLineRequest line : request.items()) {
            MenuItem menuItem = menuItemRepository.findByIdAndRestaurant_Id(line.menuItemId(), request.restaurantId())
                .orElseThrow(() -> new NotFoundException("Menu item not found"));

            if (!menuItem.isAvailable()) {
                throw new BusinessException("Menu item is not available: " + menuItem.getName());
            }
            if (menuItem.getStock() < line.quantity()) {
                throw new BusinessException("Insufficient stock for item: " + menuItem.getName());
            }

            menuItem.setStock(menuItem.getStock() - line.quantity());
            BigDecimal lineTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(line.quantity()));
            order.addItem(new OrderItem(
                menuItem.getId(),
                menuItem.getName(),
                line.quantity(),
                menuItem.getPrice(),
                lineTotal
            ));
            total = total.add(lineTotal);
        }

        order.setTotalAmount(total);
        CustomerOrder savedOrder = customerOrderRepository.save(order);

        eventPublisher.publishEvent(new OrderStatusChangedEvent(
            savedOrder.getId(),
            savedOrder.getCustomerUsername(),
            savedOrder.getRestaurant().getOwnerUsername(),
            null,
            savedOrder.getStatus()
        ));

        return apiMapper.toOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> customerOrders() {
        return customerOrderRepository.findByCustomerUsername(currentUserService.username()).stream()
            .map(apiMapper::toOrderResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse customerOrder(Long orderId) {
        CustomerOrder order = customerOwnedOrder(orderId);
        return apiMapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public CustomerOrder customerOwnedOrder(Long orderId) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found"));
        if (!order.getCustomerUsername().equals(currentUserService.username())) {
            throw new ForbiddenException("You do not own this order");
        }
        return order;
    }
}
