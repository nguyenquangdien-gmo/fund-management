package com.huybq.fund_management.domain.invoice;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record InvoiceDTO(
        String name,
        String fundType,
        String invoiceType,
        String description,
        @NotNull(message = "userId is required") Long userId,
        @NotNull(message = "Amount is required") BigDecimal amount
) {
}
