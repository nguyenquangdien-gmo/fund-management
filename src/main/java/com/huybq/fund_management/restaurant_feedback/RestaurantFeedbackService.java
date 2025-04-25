package com.huybq.fund_management.restaurant_feedback;

import com.huybq.fund_management.domain.restaurant.Restaurant;
import com.huybq.fund_management.domain.restaurant.RestaurantRepository;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RestaurantFeedbackService {

    private final RestaurantFeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public void submitFeedback(Long userId, RestaurantFeedbackRequestDto restaurantFeedback) {
        Restaurant restaurant = restaurantRepository.findById(restaurantFeedback.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        feedbackRepository.findByUserIdAndRestaurantId(userId, restaurantFeedback.getRestaurantId())
                .ifPresentOrElse(existing -> {
                    existing.setFeedbackType(restaurantFeedback.getFeedbackType());
                    existing.setFeedbackAt(LocalDateTime.now());
                    feedbackRepository.save(existing);
                }, () -> {
                    RestaurantFeedback feedback = new RestaurantFeedback();
                    feedback.setUser(user);
                    feedback.setRestaurant(restaurant);
                    feedback.setFeedbackType(restaurantFeedback.getFeedbackType());
                    feedback.setFeedbackAt(LocalDateTime.now());
                    feedbackRepository.save(feedback);
                });

        updateRestaurantBlacklist(restaurant);
    }
    private void updateRestaurantBlacklist(Restaurant restaurant) {
        Long restaurantId = restaurant.getId();

        long likes = feedbackRepository.countByRestaurantIdAndFeedbackType(restaurantId, 1);
        long dislikes = feedbackRepository.countByRestaurantIdAndFeedbackType(restaurantId, -1);

        boolean shouldBeBlacklisted = dislikes > likes;
        boolean shouldBeWhitelisted = likes > dislikes;

        if (shouldBeBlacklisted && !restaurant.isBlacklisted()) {
            restaurant.setBlacklisted(true);
            restaurantRepository.save(restaurant);
        } else if (shouldBeWhitelisted && restaurant.isBlacklisted()) {
            restaurant.setBlacklisted(false);
            restaurantRepository.save(restaurant);
        }
    }

}

