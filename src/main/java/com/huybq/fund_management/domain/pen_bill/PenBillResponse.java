package com.huybq.fund_management.domain.pen_bill;

import com.huybq.fund_management.domain.penalty.Penalty;
import com.huybq.fund_management.domain.penalty.PenaltyDTO;
import com.huybq.fund_management.domain.user.dto.UserDto;
import com.huybq.fund_management.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PenBillResponse {
    private Long id;

    private UserDto userDto;

    private PenaltyDTO penalty;

    private LocalDate dueDate;

    private BigDecimal amount;

    private String description;

    private PenBill.Status paymentStatus;
}
