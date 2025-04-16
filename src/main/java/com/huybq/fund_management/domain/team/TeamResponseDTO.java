package com.huybq.fund_management.domain.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class TeamResponseDTO {
    private String id;
    private String name;
    private String slug;
}
