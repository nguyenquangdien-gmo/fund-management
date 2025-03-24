package com.huybq.fund_management.domain.user.dto;

import com.huybq.fund_management.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDebtDTO {
    private User user;
    private BigDecimal amountToPay;
}
