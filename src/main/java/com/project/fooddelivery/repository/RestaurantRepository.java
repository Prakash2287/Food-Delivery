package com.project.fooddelivery.repository;

import com.project.fooddelivery.domain.Restaurant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByCity_NameIgnoreCase(String cityName);
    List<Restaurant> findByOwnerUsername(String ownerUsername);
}
