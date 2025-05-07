package com.huybq.fund_management.domain.order_feedback;

import lombok.*;

@Data
@Builder
public class OrderItemVoteResponseDTO {
    private Long id;
    private Long orderItemId;
    private Long userId;
    private int rating;
    private String note;
}

