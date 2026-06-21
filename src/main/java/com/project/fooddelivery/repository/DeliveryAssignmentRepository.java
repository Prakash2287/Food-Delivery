package com.project.fooddelivery.repository;

import com.project.fooddelivery.domain.DeliveryAssignment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {
    Optional<DeliveryAssignment> findByOrder_Id(Long orderId);
    List<DeliveryAssignment> findByPartner_Username(String username);
    List<DeliveryAssignment> findByPartnerIsNull();
}
