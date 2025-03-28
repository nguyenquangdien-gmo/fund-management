package com.huybq.fund_management.domain.team;

import com.huybq.fund_management.domain.late.Late;
import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository repository;
    private final TeamMapper mapper;
    private final UserRepository userRepository;

    public List<TeamDTO> getMembers(String name) {
        return repository.findUsersByName(name).stream()
                .map(mapper::toDTO)
                .toList();
    }

    public TeamDTO createTeam(TeamDTO teamCreateDTO) {
        Team team = mapper.toEntity(teamCreateDTO);
        repository.save(team);
        return mapper.toDTO(team);
    }

    public TeamDTO updateTeam(String slug, TeamDTO teamUpdateDTO) {
        Team team = repository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with slug: " + slug));
        mapper.updateEntityFromDTO(teamUpdateDTO, team);
        repository.save(team);
        return mapper.toDTO(team);
    }

    public Team getTeamBySlug(String slug) {
        return repository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with slug: " + slug));
    }

}
