package com.huybq.fund_management.domain.user;

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
    private UserResponseDTO user;
    private BigDecimal amountToPay;

    public UserDebtDTO(User user, BigDecimal amountToPay) {
        this.user = new UserResponseDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().getName(),
                user.getPhone(),
                user.getPosition(),
                user.getTeam().getName(),
                user.getDob().toString(),
                user.getJoinDate().toString()
        );
        this.amountToPay = amountToPay;
    }
}
