package com.huybq.fund_management.domain.fund;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FundDTO(
        String name,
        String description,
        String type,
        BigDecimal amount
) {
}
