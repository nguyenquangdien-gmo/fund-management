package com.huybq.fund_management.restaurant_feedback;

public class RestaurantFeedbackMapper {

    public static RestaurantFeedbackResponseDto toDto(RestaurantFeedback entity) {
        RestaurantFeedbackResponseDto dto = new RestaurantFeedbackResponseDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId());
        dto.setRestaurantId(entity.getRestaurant().getId());
        dto.setFeedbackType(entity.getFeedbackType());
        dto.setFeedbackAt(entity.getFeedbackAt());
        return dto;
    }
}
