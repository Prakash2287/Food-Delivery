package com.project.fooddelivery.repository;

import com.project.fooddelivery.domain.Review;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByOrder_Id(Long orderId);
}
