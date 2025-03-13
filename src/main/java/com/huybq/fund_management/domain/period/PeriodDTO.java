package com.huybq.fund_management.domain.period;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record PeriodDTO(
        Long id,
        Integer month,
        Integer year,
        LocalDate deadline,
        String description
) {
}
