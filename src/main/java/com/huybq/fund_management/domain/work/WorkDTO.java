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
    private LocalDate fromDate;
    private LocalDate toDate;
    private StatusType type;
    private String timePeriod;
    private String reason;
}
