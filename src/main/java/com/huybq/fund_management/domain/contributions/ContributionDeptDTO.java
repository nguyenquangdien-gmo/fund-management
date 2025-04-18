package com.huybq.fund_management.domain.contributions;

import com.huybq.fund_management.domain.user.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ContributionDeptDTO {
    private UserResponseDTO user;
    private int month;
    private int year;
    private BigDecimal amountToPay;
}
