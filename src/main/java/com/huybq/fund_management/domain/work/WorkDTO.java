package com.huybq.fund_management.domain.work;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class WorkDTO {
    private Long userId;
    private LocalDate fromDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private StatusType type;
    private String timePeriod;
    private String reason;
    private String idCreate;
}
