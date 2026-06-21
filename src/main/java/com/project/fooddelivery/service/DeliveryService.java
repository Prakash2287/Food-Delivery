package com.project.fooddelivery.service;

import com.project.fooddelivery.domain.AssignmentStatus;
import com.project.fooddelivery.domain.CustomerOrder;
import com.project.fooddelivery.domain.DeliveryAssignment;
import com.project.fooddelivery.domain.DeliveryPartner;
import com.project.fooddelivery.domain.OrderStatus;
import com.project.fooddelivery.dto.DeliveryAssignmentResponse;
import com.project.fooddelivery.dto.OrderResponse;
import com.project.fooddelivery.exception.BusinessException;
import com.project.fooddelivery.exception.ForbiddenException;
import com.project.fooddelivery.exception.NotFoundException;
import com.project.fooddelivery.repository.CustomerOrderRepository;
import com.project.fooddelivery.repository.DeliveryAssignmentRepository;
import com.project.fooddelivery.repository.DeliveryPartnerRepository;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryService {

    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final CurrentUserService currentUserService;
    private final ApiMapper apiMapper;
    private final ApplicationEventPublisher eventPublisher;

    public DeliveryService(
        DeliveryAssignmentRepository deliveryAssignmentRepository,
        DeliveryPartnerRepository deliveryPartnerRepository,
        CustomerOrderRepository customerOrderRepository,
        CurrentUserService currentUserService,
        ApiMapper apiMapper,
        ApplicationEventPublisher eventPublisher
    ) {
        this.deliveryAssignmentRepository = deliveryAssignmentRepository;
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.currentUserService = currentUserService;
        this.apiMapper = apiMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<DeliveryAssignmentResponse> availableAssignments() {
        return deliveryAssignmentRepository.findByPartnerIsNull().stream()
            .map(apiMapper::toAssignmentResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<DeliveryAssignmentResponse> myAssignments() {
        return deliveryAssignmentRepository.findByPartner_Username(currentUserService.username()).stream()
            .map(apiMapper::toAssignmentResponse)
            .toList();
    }

    @Transactional
    public DeliveryAssignmentResponse acceptAssignment(Long assignmentId) {
        DeliveryAssignment assignment = deliveryAssignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new NotFoundException("Assignment not found"));
        if (assignment.getPartner() != null) {
            throw new BusinessException("Assignment already claimed");
        }
        DeliveryPartner partner = deliveryPartnerRepository.findByUsername(currentUserService.username())
            .orElseThrow(() -> new NotFoundException("Delivery partner profile not found"));

        String orderCity = assignment.getOrder().getRestaurant().getCity().getName();
        if (!partner.getCityName().equalsIgnoreCase(orderCity)) {
            throw new ForbiddenException("Partner cannot accept assignments outside their city");
        }

        assignment.setPartner(partner);
        assignment.setStatus(AssignmentStatus.ACCEPTED);
        publishOrderEvent(assignment.getOrder(), partner.getUsername());
        return apiMapper.toAssignmentResponse(assignment);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        if (status != OrderStatus.OUT_FOR_DELIVERY && status != OrderStatus.DELIVERED) {
            throw new BusinessException("Delivery partner can only set OUT_FOR_DELIVERY or DELIVERED");
        }

        CustomerOrder order = customerOrderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found"));
        DeliveryAssignment assignment = deliveryAssignmentRepository.findByOrder_Id(orderId)
            .orElseThrow(() -> new NotFoundException("Assignment not found for order"));

        if (assignment.getPartner() == null || !assignment.getPartner().getUsername().equals(currentUserService.username())) {
            throw new ForbiddenException("You do not own this assignment");
        }
        if (status == OrderStatus.OUT_FOR_DELIVERY && order.getStatus() != OrderStatus.PREPARING) {
            throw new BusinessException("Order must be PREPARING before going out for delivery");
        }
        if (status == OrderStatus.DELIVERED && order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            throw new BusinessException("Order must be OUT_FOR_DELIVERY before delivery");
        }

        order.setStatus(status);
        publishOrderEvent(order, assignment.getPartner().getUsername());
        return apiMapper.toOrderResponse(order);
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
}
