package com.huybq.fund_management.domain.team;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamDTO {
    private String id;
    private String name;
    private String slug;
    private String channelId;
    private String qrCode;
    private String token;
}
