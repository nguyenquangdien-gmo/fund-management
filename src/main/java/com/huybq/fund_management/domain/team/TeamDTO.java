package com.huybq.fund_management.domain.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamDTO {
    private String id;
    private String name;
    private String slug;
    private String channelId;
    private String token;
    private String regulation;
}
