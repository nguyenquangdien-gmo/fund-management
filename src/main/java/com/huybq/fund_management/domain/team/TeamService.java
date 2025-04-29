package com.huybq.fund_management.domain.team;

import com.huybq.fund_management.domain.schedule.Schedule;
import com.huybq.fund_management.domain.schedule.ScheduleRepository;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository repository;
    private final TeamMapper mapper;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;

    public List<TeamResponseDTO> getTeams() {
        return repository.findAll().stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    public List<TeamResponseDTO> getMembers(String name) {
        return repository.findUsersByName(name).stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    public TeamResponseDTO createTeam(TeamDTO teamCreateDTO, MultipartFile qrCode) throws IOException {
        Team team = mapper.toEntity(teamCreateDTO);

        // Handle QR code upload
        if (qrCode != null && !qrCode.isEmpty()) {
            team.setQrCode(qrCode.getBytes());
        }

        repository.save(team);
        return mapper.toResponseDTO(team);
    }

    public TeamDTO updateTeam(String slug, TeamDTO teamUpdateDTO, MultipartFile qrCode) throws IOException {
        Team team = repository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with slug: " + slug));
        System.out.println(teamUpdateDTO.toString());
        //if not have token and channel id than set to old value
        System.out.println(teamUpdateDTO.toString());
        if (teamUpdateDTO.getToken()==null) {
            teamUpdateDTO.setToken(team.getToken());
        }
        if (teamUpdateDTO.getChannelId()==null) {
            teamUpdateDTO.setChannelId(team.getChannelId());
        }

        mapper.updateEntityFromDTO(teamUpdateDTO, team);

        // Handle QR code upload
        if (qrCode != null && !qrCode.isEmpty()) {
            team.setQrCode(qrCode.getBytes());
        }

        repository.save(team);
        return mapper.toDTO(team);
    }

    public Team getTeamBySlug(String slug) {
        return repository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with slug: " + slug));
    }

    // Utility method to get the QR code image
    public byte[] getQrCode(String slug) {
        Team team = repository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with slug: " + slug));
        return team.getQrCode();
    }

    public void deleteTeam(String slug) {
        Team team = repository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with slug: " + slug));
        repository.delete(team);
    }

    public TeamResponseDTO getTeamByUserId(Long userId) {
        return userRepository.findByIdAndIsDeleteIsFalse(userId)
                .map(user -> mapper.toResponseDTO(user.getTeam()))
                .orElseThrow(() -> new RuntimeException("User không tồn tại hoặc đã bị xóa"));
    }
}