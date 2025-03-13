package com.huybq.fund_management.domain.balance;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
@Builder
public record BalanceDTO(
        @NotNull(message = "Title is required") String title,
        BigDecimal totalAmount
) {
}
