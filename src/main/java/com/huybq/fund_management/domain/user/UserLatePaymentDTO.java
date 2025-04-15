package com.huybq.fund_management.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLatePaymentDTO {
    private User user;
    private BigDecimal totalAmount;
    private LocalDateTime paymentDate;
}
