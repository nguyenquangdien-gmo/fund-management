package com.huybq.fund_management.domain.work;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class WorkUpdateDTO {
    private Long userId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private StatusType type;
    private String timePeriod;
    private String reason;
}

