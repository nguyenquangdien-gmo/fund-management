package com.huybq.fund_management.domain.pen_bill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BillStatisticsDTO {
    private Integer year;
    private BigDecimal totalAmount;
}
