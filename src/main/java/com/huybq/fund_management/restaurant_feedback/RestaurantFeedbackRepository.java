package com.huybq.fund_management.restaurant_feedback;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantFeedbackRepository extends JpaRepository<RestaurantFeedback, Long> {
    Optional<RestaurantFeedback> findByUserIdAndRestaurantId(Long userId, Long restaurantId);
    long countByRestaurantIdAndFeedbackType(Long restaurantId, int feedbackType);

}
