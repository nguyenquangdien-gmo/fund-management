package com.huybq.fund_management.domain.pen_bill;

import com.huybq.fund_management.domain.penalty.PenaltyDTO;
import com.huybq.fund_management.domain.user.UserDTO;
import com.huybq.fund_management.domain.user.UserResponseDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PenBillResponse {
    private Long id;

    private UserResponseDTO user;

    private PenaltyDTO penalty;

    private LocalDate dueDate;

    private BigDecimal amount;

    private String description;

    private PenBill.Status paymentStatus;
    private LocalDateTime createdAt;
}
