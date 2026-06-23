package com.project.fooddelivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.fooddelivery.domain.City;
import com.project.fooddelivery.domain.CustomerOrder;
import com.project.fooddelivery.domain.MenuItem;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private CustomerOrderRepository customerOrderRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
            restaurantRepository,
            menuItemRepository,
            customerOrderRepository,
            currentUserService,
            new ApiMapper(),
            eventPublisher
        );
    }

    @Test
    void placeOrderDeductsStockAndCreatesPaidOrder() {
        Restaurant restaurant = restaurant();
        MenuItem menuItem = new MenuItem("Paneer Roll", new BigDecimal("120.00"), 10, restaurant);
        PlaceOrderRequest request = request(2);

        when(currentUserService.username()).thenReturn("customer1");
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findByIdAndRestaurant_Id(5L, 1L)).thenReturn(Optional.of(menuItem));
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.placeOrder(request);

        assertThat(menuItem.getStock()).isEqualTo(8);
        assertThat(response.customerUsername()).isEqualTo("customer1");
        assertThat(response.totalAmount()).isEqualByComparingTo("240.00");
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(response.status()).isEqualTo(OrderStatus.PLACED);
        assertThat(response.items()).hasSize(1);
        verify(eventPublisher).publishEvent(any(OrderStatusChangedEvent.class));
    }

    @Test
    void placeOrderFailsWhenStockIsInsufficient() {
        Restaurant restaurant = restaurant();
        MenuItem menuItem = new MenuItem("Paneer Roll", new BigDecimal("120.00"), 1, restaurant);

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findByIdAndRestaurant_Id(5L, 1L)).thenReturn(Optional.of(menuItem));

        assertThatThrownBy(() -> orderService.placeOrder(request(2)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Insufficient stock");

        verify(customerOrderRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void placeOrderFailsWhenMenuItemIsUnavailable() {
        Restaurant restaurant = restaurant();
        MenuItem menuItem = new MenuItem("Paneer Roll", new BigDecimal("120.00"), 10, restaurant);
        menuItem.setAvailable(false);

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findByIdAndRestaurant_Id(5L, 1L)).thenReturn(Optional.of(menuItem));

        assertThatThrownBy(() -> orderService.placeOrder(request(1)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("not available");

        assertThat(menuItem.getStock()).isEqualTo(10);
        verify(customerOrderRepository, never()).save(any());
    }

    @Test
    void placeOrderFailsWhenRestaurantDoesNotExist() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(request(1)))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Restaurant not found");

        verify(menuItemRepository, never()).findByIdAndRestaurant_Id(any(), any());
        verify(customerOrderRepository, never()).save(any());
    }

    @Test
    void customerOwnedOrderReturnsOrderForCurrentCustomer() {
        CustomerOrder order = order("customer1", OrderStatus.PLACED);
        when(customerOrderRepository.findById(9L)).thenReturn(Optional.of(order));
        when(currentUserService.username()).thenReturn("customer1");

        CustomerOrder result = orderService.customerOwnedOrder(9L);

        assertThat(result).isSameAs(order);
    }

    @Test
    void customerOwnedOrderRejectsDifferentCustomer() {
        CustomerOrder order = order("customer1", OrderStatus.PLACED);
        when(customerOrderRepository.findById(9L)).thenReturn(Optional.of(order));
        when(currentUserService.username()).thenReturn("customer2");

        assertThatThrownBy(() -> orderService.customerOwnedOrder(9L))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("You do not own this order");
    }

    private PlaceOrderRequest request(int quantity) {
        return new PlaceOrderRequest(1L, List.of(new OrderLineRequest(5L, quantity)));
    }

    private Restaurant restaurant() {
        return new Restaurant("Spice Hub", "owner1", new City("Delhi"));
    }

    private CustomerOrder order(String customer, OrderStatus status) {
        return new CustomerOrder(customer, status, PaymentStatus.PAID, restaurant());
    }
}
