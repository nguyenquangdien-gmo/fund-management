package com.huybq.fund_management.domain.work;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkResponseDTO {
    private Long id;
    private Long userId;
    private String fullName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private StatusType type;
    private TimePeriod timePeriod;
    private String reason;
    private String status;
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime createdAt;
}
