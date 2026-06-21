package com.project.fooddelivery.repository;

import com.project.fooddelivery.domain.MenuItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurant_IdAndAvailableTrue(Long restaurantId);
    Optional<MenuItem> findByIdAndRestaurant_Id(Long id, Long restaurantId);
}
