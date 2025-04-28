package com.huybq.fund_management.domain.order_item_feedback;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemFeedbackResponseDTO {
    private Long id;
    private Long userId;
    private Long orderItemId;
    private int feedbackType;
}
