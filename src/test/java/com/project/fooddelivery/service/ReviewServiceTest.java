package com.project.fooddelivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.fooddelivery.domain.City;
import com.project.fooddelivery.domain.CustomerOrder;
import com.project.fooddelivery.domain.OrderStatus;
import com.project.fooddelivery.domain.PaymentStatus;
import com.project.fooddelivery.domain.Restaurant;
import com.project.fooddelivery.domain.Review;
import com.project.fooddelivery.dto.CreateReviewRequest;
import com.project.fooddelivery.dto.ReviewResponse;
import com.project.fooddelivery.exception.BusinessException;
import com.project.fooddelivery.repository.ReviewRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private OrderService orderService;

    @Mock
    private ReviewRepository reviewRepository;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(orderService, reviewRepository, new ApiMapper());
    }

    @Test
    void createReviewSavesReviewForDeliveredOrder() {
        CustomerOrder order = order(OrderStatus.DELIVERED);
        CreateReviewRequest request = new CreateReviewRequest(5, "Excellent");
        when(orderService.customerOwnedOrder(7L)).thenReturn(order);
        when(reviewRepository.findByOrder_Id(7L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse response = reviewService.createReview(7L, request);

        assertThat(response.customerUsername()).isEqualTo("customer1");
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.comment()).isEqualTo("Excellent");
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReviewFailsBeforeOrderIsDelivered() {
        when(orderService.customerOwnedOrder(7L)).thenReturn(order(OrderStatus.OUT_FOR_DELIVERY));

        assertThatThrownBy(() -> reviewService.createReview(7L, new CreateReviewRequest(4, "Good")))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Review can only be added after delivery");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReviewFailsWhenReviewAlreadyExists() {
        CustomerOrder order = order(OrderStatus.DELIVERED);
        Review existing = new Review(order, "customer1", 4, "Already reviewed");
        when(orderService.customerOwnedOrder(7L)).thenReturn(order);
        when(reviewRepository.findByOrder_Id(7L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> reviewService.createReview(7L, new CreateReviewRequest(5, "Again")))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Review already exists for this order");

        verify(reviewRepository, never()).save(any());
    }

    private CustomerOrder order(OrderStatus status) {
        Restaurant restaurant = new Restaurant("Spice Hub", "owner1", new City("Delhi"));
        return new CustomerOrder("customer1", status, PaymentStatus.PAID, restaurant);
    }
}
