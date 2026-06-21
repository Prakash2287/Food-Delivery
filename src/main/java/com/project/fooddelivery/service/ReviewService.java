package com.project.fooddelivery.service;

import com.project.fooddelivery.domain.CustomerOrder;
import com.project.fooddelivery.domain.OrderStatus;
import com.project.fooddelivery.domain.Review;
import com.project.fooddelivery.dto.CreateReviewRequest;
import com.project.fooddelivery.dto.ReviewResponse;
import com.project.fooddelivery.exception.BusinessException;
import com.project.fooddelivery.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final OrderService orderService;
    private final ReviewRepository reviewRepository;
    private final ApiMapper apiMapper;

    public ReviewService(OrderService orderService, ReviewRepository reviewRepository, ApiMapper apiMapper) {
        this.orderService = orderService;
        this.reviewRepository = reviewRepository;
        this.apiMapper = apiMapper;
    }

    @Transactional
    public ReviewResponse createReview(Long orderId, CreateReviewRequest request) {
        CustomerOrder order = orderService.customerOwnedOrder(orderId);
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException("Review can only be added after delivery");
        }
        reviewRepository.findByOrder_Id(orderId).ifPresent(review -> {
            throw new BusinessException("Review already exists for this order");
        });

        Review review = reviewRepository.save(
            new Review(order, order.getCustomerUsername(), request.rating(), request.comment())
        );
        return apiMapper.toReviewResponse(review);
    }
}
