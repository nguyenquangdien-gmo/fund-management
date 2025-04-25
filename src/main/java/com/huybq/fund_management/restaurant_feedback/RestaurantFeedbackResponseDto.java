package com.huybq.fund_management.restaurant_feedback;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RestaurantFeedbackResponseDto {
    private Long id;
    private Long userId;
    private Long restaurantId;
    private int feedbackType;
    private LocalDateTime feedbackAt;
}

