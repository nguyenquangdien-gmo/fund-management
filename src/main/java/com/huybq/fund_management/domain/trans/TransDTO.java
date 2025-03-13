package com.huybq.fund_management.domain.trans;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Builder
public class TransDTO {
    private Long userId;
    private Long periodId;
    private BigDecimal amount;
    private Trans.TransactionType transactionType;
    private String description;
    private LocalDateTime createdAt;

}
