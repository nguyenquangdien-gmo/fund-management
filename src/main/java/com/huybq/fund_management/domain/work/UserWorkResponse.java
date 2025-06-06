package com.huybq.fund_management.domain.work;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserWorkResponse {
    private Long userId;
    private String fullName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String type;
    private LocalTime startTime;
    private LocalTime endTime;
}
