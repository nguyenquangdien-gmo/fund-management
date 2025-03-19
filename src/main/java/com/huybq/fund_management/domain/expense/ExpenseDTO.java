package com.huybq.fund_management.domain.expense;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ExpenseDTO(
        Long id,
        String name,
        String expenseType,
        String description,
        @NotNull(message = "userId is required") Long userId,
        @NotNull(message = "Amount is required") BigDecimal amount,
        LocalDateTime createdAt
) {
}
