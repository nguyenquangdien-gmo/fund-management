package com.huybq.fund_management.domain.pen_bill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PenBillDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Penalty ID is required")
    private Long penaltyId;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private BigDecimal amount;

    @NotBlank(message = "Description cannot be empty")
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private PenBill.Status paymentStatus;
}

