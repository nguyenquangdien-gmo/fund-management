package com.huybq.fund_management.domain.late;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLateCountDTO {
    private Long userId;
    private String fullName;
    private int lateCount;
}
