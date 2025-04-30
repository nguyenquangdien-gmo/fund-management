package com.huybq.fund_management.domain.pen_bill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PenBillDTO {
    private Long id;

    private Long userId;

    @NotNull(message = "Penalty slug is required")
    private String penaltySlug;

    private LocalDate dueDate;

    private BigDecimal amount;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private String paymentStatus;

    private LocalDateTime createdAt;

    List<Long> userIds;
}

