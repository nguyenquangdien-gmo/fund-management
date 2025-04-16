package com.huybq.fund_management.domain.team;

import com.huybq.fund_management.domain.user.UserService;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final UserService userService;
    private final TeamMapper mapper;

    @GetMapping
    public ResponseEntity<List<TeamResponseDTO>> getAllTeams() {
        List<TeamResponseDTO> teams = teamService.getTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<TeamResponseDTO> getTeamBySlug(@PathVariable String slug) {
        Team team = teamService.getTeamBySlug(slug);
        return ResponseEntity.ok(mapper.toResponseDTO(team));
    }

    @GetMapping("/members/{name}")
    public ResponseEntity<List<TeamResponseDTO>> getTeamMembers(@PathVariable String name) {
        List<TeamResponseDTO> members = teamService.getMembers(name);
        return ResponseEntity.ok(members);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createTeam(
            @RequestPart("team") TeamDTO teamDTO,
            @RequestPart(value = "qrCode", required = false) MultipartFile qrCode) {
        try {
            TeamResponseDTO createdTeam = teamService.createTeam(teamDTO, qrCode);
            return new ResponseEntity<>(createdTeam, HttpStatus.CREATED);
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing QR code image: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{slug}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateTeam(
            @PathVariable String slug,
            @RequestPart("team") TeamDTO teamDTO,
            @RequestPart(value = "qrCode", required = false) MultipartFile qrCode) {
        try {
            TeamDTO updatedTeam = teamService.updateTeam(slug, teamDTO, qrCode);
            return ResponseEntity.ok(updatedTeam);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing QR code image: " + e.getMessage());
        }
    }

    @GetMapping("/{slug}/qrcode")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String slug) {
        try {
            byte[] qrCodeData = teamService.getQrCode(slug);
            if (qrCodeData == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qrCodeData);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> deleteTeam(@PathVariable String slug) {
        teamService.deleteTeam(slug);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/team")
    public ResponseEntity<Team> getTeamByUserId(@PathVariable Long userId) {
        Team team = teamService.getTeamByUserId(userId);
        return ResponseEntity.ok(team);
    }
}