package com.huybq.fund_management.domain.contributions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
public class ContributionStatsDTO {
    private Integer year;
    private BigDecimal totalAmount;
}
