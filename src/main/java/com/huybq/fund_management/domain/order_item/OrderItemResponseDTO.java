package com.huybq.fund_management.domain.order_item;

import com.huybq.fund_management.domain.user.UserResponseDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderItemResponseDTO {
    private Long id;
    private Long orderId;
    private UserResponseDTO user;
    private String itemName;
    private String size;
    private String sugar;
    private String ice;
    private String topping;
    private String note;
    private LocalDateTime createdAt;
}
