package com.huybq.fund_management.domain.order_feedback;


import lombok.Data;

@Data
public class OrderItemVoteRequestDTO {
    private Long orderItemId; // ID của OrderItem
    private int rating;      // Đánh giá từ 1 đến 5 sao
    private String note;     // Optional note
}


