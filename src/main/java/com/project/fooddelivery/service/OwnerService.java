package com.project.fooddelivery.service;

import com.project.fooddelivery.domain.AssignmentStatus;
import com.project.fooddelivery.domain.CustomerOrder;
import com.project.fooddelivery.domain.DeliveryAssignment;
import com.project.fooddelivery.domain.MenuItem;
import com.project.fooddelivery.domain.OrderStatus;
import com.project.fooddelivery.domain.Restaurant;
import com.project.fooddelivery.dto.CreateMenuItemRequest;
import com.project.fooddelivery.dto.MenuItemResponse;
import com.project.fooddelivery.dto.OrderResponse;
import com.project.fooddelivery.dto.RestaurantResponse;
import com.project.fooddelivery.dto.UpdateMenuItemStockRequest;
import com.project.fooddelivery.exception.BusinessException;
import com.project.fooddelivery.exception.ForbiddenException;
import com.project.fooddelivery.exception.NotFoundException;
import com.project.fooddelivery.repository.CustomerOrderRepository;
import com.project.fooddelivery.repository.DeliveryAssignmentRepository;
import com.project.fooddelivery.repository.MenuItemRepository;
import com.project.fooddelivery.repository.RestaurantRepository;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OwnerService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final CurrentUserService currentUserService;
    private final ApiMapper apiMapper;
    private final ApplicationEventPublisher eventPublisher;

    public OwnerService(
        RestaurantRepository restaurantRepository,
        MenuItemRepository menuItemRepository,
        CustomerOrderRepository customerOrderRepository,
        DeliveryAssignmentRepository deliveryAssignmentRepository,
        CurrentUserService currentUserService,
        ApiMapper apiMapper,
        ApplicationEventPublisher eventPublisher
    ) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.deliveryAssignmentRepository = deliveryAssignmentRepository;
        this.currentUserService = currentUserService;
        this.apiMapper = apiMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> ownedRestaurants() {
        return restaurantRepository.findByOwnerUsername(currentUserService.username()).stream()
            .map(apiMapper::toRestaurantResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> ownedOrders() {
        return customerOrderRepository.findByRestaurant_OwnerUsername(currentUserService.username()).stream()
            .map(apiMapper::toOrderResponse)
            .toList();
    }

    @Transactional
    public MenuItemResponse createMenuItem(Long restaurantId, CreateMenuItemRequest request) {
        Restaurant restaurant = ownedRestaurant(restaurantId);
        MenuItem menuItem = menuItemRepository.save(
            new MenuItem(request.name(), request.price(), request.stock(), restaurant)
        );
        return apiMapper.toMenuItemResponse(menuItem);
    }

    @Transactional
    public MenuItemResponse updateStock(Long restaurantId, Long menuItemId, UpdateMenuItemStockRequest request) {
        ownedRestaurant(restaurantId);
        MenuItem menuItem = menuItemRepository.findByIdAndRestaurant_Id(menuItemId, restaurantId)
            .orElseThrow(() -> new NotFoundException("Menu item not found"));
        menuItem.setStock(request.stock());
        menuItem.setAvailable(request.available());
        return apiMapper.toMenuItemResponse(menuItem);
    }

    @Transactional
    public OrderResponse decideOrder(Long orderId, boolean accepted) {
        CustomerOrder order = ownedOrder(orderId);
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new BusinessException("Only placed orders can be accepted or rejected");
        }

        if (accepted) {
            order.setStatus(OrderStatus.ACCEPTED);
            deliveryAssignmentRepository.save(new DeliveryAssignment(order, AssignmentStatus.PENDING));
        } else {
            order.setStatus(OrderStatus.REJECTED);
            order.getItems().forEach(item -> {
                MenuItem menuItem = menuItemRepository.findById(item.getMenuItemId())
                    .orElseThrow(() -> new NotFoundException("Menu item not found for restock"));
                menuItem.setStock(menuItem.getStock() + item.getQuantity());
            });
        }

        publishOrderEvent(order, null);
        return apiMapper.toOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        CustomerOrder order = ownedOrder(orderId);
        if (status != OrderStatus.PREPARING) {
            throw new BusinessException("Restaurant owner can only move orders to PREPARING here");
        }
        if (order.getStatus() != OrderStatus.ACCEPTED) {
            throw new BusinessException("Only accepted orders can move to preparing");
        }
        order.setStatus(status);
        publishOrderEvent(order, currentPartnerUsername(order.getId()));
        return apiMapper.toOrderResponse(order);
    }

    private Restaurant ownedRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant not found"));
        if (!restaurant.getOwnerUsername().equals(currentUserService.username())) {
            throw new ForbiddenException("You do not own this restaurant");
        }
        return restaurant;
    }

    private CustomerOrder ownedOrder(Long orderId) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found"));
        if (!order.getRestaurant().getOwnerUsername().equals(currentUserService.username())) {
            throw new ForbiddenException("You do not own this order");
        }
        return order;
    }

    private void publishOrderEvent(CustomerOrder order, String partnerUsername) {
        eventPublisher.publishEvent(new OrderStatusChangedEvent(
            order.getId(),
            order.getCustomerUsername(),
            order.getRestaurant().getOwnerUsername(),
            partnerUsername,
            order.getStatus()
        ));
    }

    private String currentPartnerUsername(Long orderId) {
        return deliveryAssignmentRepository.findByOrder_Id(orderId)
            .map(assignment -> assignment.getPartner() == null ? null : assignment.getPartner().getUsername())
            .orElse(null);
    }
}
