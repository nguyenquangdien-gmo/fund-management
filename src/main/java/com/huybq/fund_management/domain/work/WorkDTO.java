package com.huybq.fund_management.domain.work;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class WorkDTO {
    private Long userId;
    private LocalDate date;
    private StatusType type;
    private String timePeriod;
    private String reason;
}
