package com.huybq.fund_management.domain.order_item_feedback;

import lombok.Data;

@Data
public class OrderItemFeedbackRequestDTO {
    private Long orderItemId;
    private int feedbackType; // 1 = like, -1 = dislike
}

