package com.huybq.fund_management.domain.self_reminder;

import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfReminderResponseDTO {
    private Long id;
    private String title;
    private String message;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalTime notifyHour;
    private Integer repeatCount;
    private Integer repeatIntervalDays;
    private String status;
    private LocalDateTime createdAt;
}
