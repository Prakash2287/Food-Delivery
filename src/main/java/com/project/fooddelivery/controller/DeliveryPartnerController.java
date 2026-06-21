package com.project.fooddelivery.controller;

import com.project.fooddelivery.dto.DeliveryAssignmentResponse;
import com.project.fooddelivery.dto.OrderResponse;
import com.project.fooddelivery.dto.UpdateOrderStatusRequest;
import com.project.fooddelivery.service.DeliveryService;
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
@RequestMapping("/api/partner")
public class DeliveryPartnerController {

    private final DeliveryService deliveryService;

    public DeliveryPartnerController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/assignments/available")
    public List<DeliveryAssignmentResponse> availableAssignments() {
        return deliveryService.availableAssignments();
    }

    @GetMapping("/assignments/me")
    public List<DeliveryAssignmentResponse> myAssignments() {
        return deliveryService.myAssignments();
    }

    @PostMapping("/assignments/{assignmentId}/accept")
    public DeliveryAssignmentResponse acceptAssignment(@PathVariable Long assignmentId) {
        return deliveryService.acceptAssignment(assignmentId);
    }

    @PatchMapping("/orders/{orderId}/status")
    public OrderResponse updateStatus(@PathVariable Long orderId, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return deliveryService.updateOrderStatus(orderId, request.status());
    }
}
