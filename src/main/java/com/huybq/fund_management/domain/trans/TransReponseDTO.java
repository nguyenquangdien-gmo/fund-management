package com.huybq.fund_management.domain.trans;

import com.huybq.fund_management.domain.user.UserDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
public class TransReponseDTO {
    private UserDTO userDto;
    private BigDecimal amount;
    private Trans.TransactionType transactionType;
    private String description;
    private LocalDateTime createdAt;

}
