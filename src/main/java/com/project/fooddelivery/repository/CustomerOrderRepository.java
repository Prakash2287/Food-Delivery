package com.project.fooddelivery.repository;

import com.project.fooddelivery.domain.CustomerOrder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByCustomerUsername(String customerUsername);
    List<CustomerOrder> findByRestaurant_OwnerUsername(String ownerUsername);
}
