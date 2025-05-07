package com.huybq.fund_management.domain.order;

import com.huybq.fund_management.domain.user.UserResponseDTO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderResponseDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private String status;
    private Long restaurantId;
    private String restaurantName;
    private String restaurantLink;
    private UserResponseDTO createdBy;
    private LocalDateTime createdAt;
}
