package com.huybq.fund_management.restaurant_feedback;

import lombok.Data;

@Data
public class RestaurantFeedbackRequestDto {
    private Long restaurantId;
    private int feedbackType; // 1 = like, -1 = dislike
}
