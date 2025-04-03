package com.huybq.fund_management.domain.team;

import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository repository;
    private final TeamMapper mapper;
    private final UserRepository userRepository;

    public List<TeamDTO> getTeams() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    public List<TeamDTO> getMembers(String name) {
        return repository.findUsersByName(name).stream()
                .map(mapper::toDTO)
                .toList();
    }

    public TeamDTO createTeam(TeamDTO teamCreateDTO, MultipartFile qrCode) throws IOException {
        Team team = mapper.toEntity(teamCreateDTO);

        // Handle QR code upload
        if (qrCode != null && !qrCode.isEmpty()) {
            team.setQrCode(qrCode.getBytes());
        }

        repository.save(team);
        return mapper.toDTO(team);
    }

    public TeamDTO updateTeam(String slug, TeamDTO teamUpdateDTO, MultipartFile qrCode) throws IOException {
        Team team = repository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with slug: " + slug));

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

    public Team getTeamByUserId(Long userId) {
        return userRepository.findByIdAndIsDeleteIsFalse(userId)
                .map(User::getTeam)
                .orElseThrow(() -> new RuntimeException("User không tồn tại hoặc đã bị xóa"));
    }
}