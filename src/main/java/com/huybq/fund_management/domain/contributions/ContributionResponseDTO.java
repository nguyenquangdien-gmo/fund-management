package com.huybq.fund_management.domain.contributions;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContributionResponseDTO {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long periodId;
    private String periodName;
    private BigDecimal totalAmount;
    private Contribution.PaymentStatus paymentStatus;
    private String note;
    private LocalDate deadline;
    private Boolean isLate;
    private LocalDateTime createdAt;
}
