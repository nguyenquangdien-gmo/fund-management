package com.huybq.fund_management.domain.trans;

import com.huybq.fund_management.domain.user.dto.UserDto;
import com.huybq.fund_management.domain.user.entity.User;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
public class TransReponseDTO {
    private UserDto userDto;
    private BigDecimal amount;
    private Trans.TransactionType transactionType;
    private String description;
    private LocalDateTime createdAt;

}
