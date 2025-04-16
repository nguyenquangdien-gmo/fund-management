package com.huybq.fund_management.domain.invoice;

import com.huybq.fund_management.domain.user.UserDTO;
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
        UserDTO user,
        BigDecimal amount,
        LocalDateTime createdAt
){
}
