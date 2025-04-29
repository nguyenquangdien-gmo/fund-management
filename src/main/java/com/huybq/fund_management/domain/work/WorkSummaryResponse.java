package com.huybq.fund_management.domain.work;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class WorkSummaryResponse {
    private Long userId;
    private String memberName;
    private List<WorkStatsResponse> work;
}
