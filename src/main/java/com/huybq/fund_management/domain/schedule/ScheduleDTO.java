package com.huybq.fund_management.domain.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleDTO {
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private LocalTime sendTime;
    private String type;
}
