package com.huybq.fund_management.domain.invoice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
public class InvoiceStatsDTO {
    private Integer year;
    private BigDecimal totalAmount;
}
