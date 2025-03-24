package com.huybq.fund_management.domain.team;

import org.springframework.stereotype.Service;

@Service
public class TeamMapper {

    public TeamDTO toDTO(Team team) {
        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .slug(team.getSlug())
                .build();
    }
}
