package com.huybq.fund_management.domain.work;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
@Builder
@Data
public class WorkStatsResponse {
    private LocalDate fromDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private StatusType type;
    private LocalDateTime createdAt;
}
