package com.huybq.fund_management.domain.invoice;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Builder
public record InvoiceResponseDTO (
        Long id,
        String name,
        String fundType,
        String invoiceType,
        String status,
        String description,
        Long userId,
        BigDecimal amount,
        LocalDateTime createdAt
){
}
