package com.huybq.fund_management.domain.team;

import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class TeamMapper {

    public TeamResponseDTO toResponseDTO(Team team) {
        return TeamResponseDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .slug(team.getSlug())
                .regulation(team.getRegulation())
                .build();
    }

    public TeamDTO toDTO(Team team) {
        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .slug(team.getSlug())
                .channelId(team.getChannelId())
                .token(team.getToken())
                .regulation(team.getRegulation())
                .build();
    }

    public Team toEntity(TeamDTO teamDTO) {
        return Team.builder()
                .id(teamDTO.getId())
                .name(teamDTO.getName())
                .slug(teamDTO.getSlug())
                .channelId(teamDTO.getChannelId())
//                .qrCode(teamDTO.getQrCode().getBytes())
                .token(teamDTO.getToken())
                .regulation(teamDTO.getRegulation())
                .build();
    }

    public void updateEntityFromDTO(TeamDTO teamUpdateDTO, Team team) {
        team.setName(teamUpdateDTO.getName());
        team.setSlug(teamUpdateDTO.getSlug());
        team.setChannelId(teamUpdateDTO.getChannelId());
//        team.setQrCode(teamUpdateDTO.getQrCode().getBytes());
        team.setToken(teamUpdateDTO.getToken());
        team.setRegulation(teamUpdateDTO.getRegulation());
    }
}
