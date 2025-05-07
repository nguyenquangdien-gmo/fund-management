package com.huybq.fund_management.domain.order;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderRequestDto {
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Long restaurantId;
    private List<Long> relatedUserIds;
}

