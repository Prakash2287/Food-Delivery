package com.project.fooddelivery.repository;

import com.project.fooddelivery.domain.DeliveryPartner;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    Optional<DeliveryPartner> findByUsername(String username);
}
