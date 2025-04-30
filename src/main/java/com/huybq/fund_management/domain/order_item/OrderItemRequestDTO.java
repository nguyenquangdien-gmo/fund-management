package com.huybq.fund_management.domain.order_item;

import lombok.Data;

@Data
public class OrderItemRequestDTO {
    private Long orderId;
    private String itemName;
    private String size;
    private String sugar;
    private String ice;
    private String topping;
    private String note;
}

