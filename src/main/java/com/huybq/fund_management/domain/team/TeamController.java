package com.huybq.fund_management.domain.team;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/${server.version}/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    // Lấy danh sách thành viên theo tên
    @GetMapping("/members")
    public ResponseEntity<List<TeamDTO>> getMembers(@RequestParam String name) {
        List<TeamDTO> members = teamService.getMembers(name);
        return ResponseEntity.ok(members);
    }

    // Tạo mới team
    @PostMapping
    public ResponseEntity<TeamDTO> createTeam(@RequestBody TeamDTO teamCreateDTO) {
        TeamDTO createdTeam = teamService.createTeam(teamCreateDTO);
        return ResponseEntity.ok(createdTeam);
    }

    // Cập nhật team theo slug
    @PutMapping("/{slug}")
    public ResponseEntity<TeamDTO> updateTeam(@PathVariable String slug, @RequestBody TeamDTO teamUpdateDTO) {
        TeamDTO updatedTeam = teamService.updateTeam(slug, teamUpdateDTO);
        return ResponseEntity.ok(updatedTeam);
    }


    // Lấy thông tin team theo slug
    @GetMapping("/{slug}")
    public ResponseEntity<Team> getTeamBySlug(@PathVariable String slug) {
        Team team = teamService.getTeamBySlug(slug);
        return ResponseEntity.ok(team);
    }
}
