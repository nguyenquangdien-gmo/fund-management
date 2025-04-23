package com.huybq.fund_management.domain.work;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserWorkResponse {
    private Long userId;
    private String fullName;
    private String type;
}
