package com.project.fooddelivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.fooddelivery.domain.AssignmentStatus;
import com.project.fooddelivery.domain.City;
import com.project.fooddelivery.domain.CustomerOrder;
import com.project.fooddelivery.domain.DeliveryAssignment;
import com.project.fooddelivery.domain.DeliveryPartner;
import com.project.fooddelivery.domain.OrderStatus;
import com.project.fooddelivery.domain.PaymentStatus;
import com.project.fooddelivery.domain.Restaurant;
import com.project.fooddelivery.dto.DeliveryAssignmentResponse;
import com.project.fooddelivery.dto.OrderResponse;
import com.project.fooddelivery.exception.BusinessException;
import com.project.fooddelivery.exception.ForbiddenException;
import com.project.fooddelivery.repository.CustomerOrderRepository;
import com.project.fooddelivery.repository.DeliveryAssignmentRepository;
import com.project.fooddelivery.repository.DeliveryPartnerRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryAssignmentRepository assignmentRepository;

    @Mock
    private DeliveryPartnerRepository partnerRepository;

    @Mock
    private CustomerOrderRepository orderRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryService(
            assignmentRepository,
            partnerRepository,
            orderRepository,
            currentUserService,
            new ApiMapper(),
            eventPublisher
        );
    }

    @Test
    void acceptAssignmentClaimsAvailableAssignmentInSameCity() {
        DeliveryAssignment assignment = assignment(OrderStatus.ACCEPTED);
        DeliveryPartner partner = new DeliveryPartner("partner1", "Ravi", "Delhi");
        when(assignmentRepository.findById(3L)).thenReturn(Optional.of(assignment));
        when(currentUserService.username()).thenReturn("partner1");
        when(partnerRepository.findByUsername("partner1")).thenReturn(Optional.of(partner));

        DeliveryAssignmentResponse response = deliveryService.acceptAssignment(3L);

        assertThat(assignment.getPartner()).isSameAs(partner);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ACCEPTED);
        assertThat(response.partnerUsername()).isEqualTo("partner1");
        verify(eventPublisher).publishEvent(any(OrderStatusChangedEvent.class));
    }

    @Test
    void acceptAssignmentFailsWhenAlreadyClaimed() {
        DeliveryAssignment assignment = assignment(OrderStatus.ACCEPTED);
        assignment.setPartner(new DeliveryPartner("partner2", "Asha", "Delhi"));
        when(assignmentRepository.findById(3L)).thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> deliveryService.acceptAssignment(3L))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Assignment already claimed");

        verify(partnerRepository, never()).findByUsername(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void acceptAssignmentRejectsPartnerFromDifferentCity() {
        DeliveryAssignment assignment = assignment(OrderStatus.ACCEPTED);
        DeliveryPartner partner = new DeliveryPartner("partner1", "Ravi", "Mumbai");
        when(assignmentRepository.findById(3L)).thenReturn(Optional.of(assignment));
        when(currentUserService.username()).thenReturn("partner1");
        when(partnerRepository.findByUsername("partner1")).thenReturn(Optional.of(partner));

        assertThatThrownBy(() -> deliveryService.acceptAssignment(3L))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("outside their city");

        assertThat(assignment.getPartner()).isNull();
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void updateOrderStatusMovesPreparingOrderOutForDelivery() {
        CustomerOrder order = order(OrderStatus.PREPARING);
        DeliveryAssignment assignment = claimedAssignment(order, "partner1");
        when(orderRepository.findById(8L)).thenReturn(Optional.of(order));
        when(assignmentRepository.findByOrder_Id(8L)).thenReturn(Optional.of(assignment));
        when(currentUserService.username()).thenReturn("partner1");

        OrderResponse response = deliveryService.updateOrderStatus(8L, OrderStatus.OUT_FOR_DELIVERY);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY);
        assertThat(response.status()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY);
        verify(eventPublisher).publishEvent(any(OrderStatusChangedEvent.class));
    }

    @Test
    void updateOrderStatusRejectsInvalidTransitionToDelivered() {
        CustomerOrder order = order(OrderStatus.PREPARING);
        DeliveryAssignment assignment = claimedAssignment(order, "partner1");
        when(orderRepository.findById(8L)).thenReturn(Optional.of(order));
        when(assignmentRepository.findByOrder_Id(8L)).thenReturn(Optional.of(assignment));
        when(currentUserService.username()).thenReturn("partner1");

        assertThatThrownBy(() -> deliveryService.updateOrderStatus(8L, OrderStatus.DELIVERED))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Order must be OUT_FOR_DELIVERY before delivery");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void updateOrderStatusRejectsPartnerWhoDoesNotOwnAssignment() {
        CustomerOrder order = order(OrderStatus.PREPARING);
        DeliveryAssignment assignment = claimedAssignment(order, "partner2");
        when(orderRepository.findById(8L)).thenReturn(Optional.of(order));
        when(assignmentRepository.findByOrder_Id(8L)).thenReturn(Optional.of(assignment));
        when(currentUserService.username()).thenReturn("partner1");

        assertThatThrownBy(() -> deliveryService.updateOrderStatus(8L, OrderStatus.OUT_FOR_DELIVERY))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("You do not own this assignment");
    }

    private DeliveryAssignment assignment(OrderStatus status) {
        return new DeliveryAssignment(order(status), AssignmentStatus.PENDING);
    }

    private DeliveryAssignment claimedAssignment(CustomerOrder order, String username) {
        DeliveryAssignment assignment = new DeliveryAssignment(order, AssignmentStatus.ACCEPTED);
        assignment.setPartner(new DeliveryPartner(username, "Delivery Partner", "Delhi"));
        return assignment;
    }

    private CustomerOrder order(OrderStatus status) {
        Restaurant restaurant = new Restaurant("Spice Hub", "owner1", new City("Delhi"));
        return new CustomerOrder("customer1", status, PaymentStatus.PAID, restaurant);
    }
}
