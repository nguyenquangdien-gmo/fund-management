package com.huybq.fund_management.domain.work;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkResponseDTO {
    private Long id;
    private Long userId;
    private String fullName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private StatusType type;
    private TimePeriod timePeriod;
    private String reason;
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime createdAt;
}
