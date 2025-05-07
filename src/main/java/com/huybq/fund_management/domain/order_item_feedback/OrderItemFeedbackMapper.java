package com.huybq.fund_management.domain.order_item_feedback;

public class OrderItemFeedbackMapper {

    public static OrderItemFeedbackResponseDTO toResponseDTO(OrderItemFeedback feedback) {
        return OrderItemFeedbackResponseDTO.builder()
                .id(feedback.getId())
                .userId(feedback.getUser().getId())
                .orderItemId(feedback.getOrderItem().getId())
                .feedbackType(feedback.getFeedbackType())
                .build();
    }
}

