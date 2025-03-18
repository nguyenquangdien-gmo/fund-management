package com.huybq.fund_management.domain.contributions;

import com.huybq.fund_management.domain.user.dto.UserDto;
import com.huybq.fund_management.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contributions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContributionController {

    private final ContributionService contributionService;


    @GetMapping
    public ResponseEntity<List<ContributionResponseDTO>> getAllContributions() {
        return ResponseEntity.ok(contributionService.getAllContributions());
    }

    @GetMapping("/users-not-contributed")
    public ResponseEntity<List<UserDto>> getUsersNotContributed(@RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(contributionService.getUsersNotContributedOrOwed(month, year));
    }

    @GetMapping("/")
    public ResponseEntity<List<ContributionResponseDTO>> getContributionById(@RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(contributionService.getAllContributionsByMonthAndYear(month, year));
    }

    // Lấy tất cả contributions của một kỳ (period)
    @GetMapping("/period/{periodId}")
    public ResponseEntity<List<ContributionResponseDTO>> getAllContributionsByPeriod(@PathVariable Long periodId) {
        List<ContributionResponseDTO> contributions = contributionService.findAllContributions(periodId);
        return ResponseEntity.ok(contributions);
    }

    @GetMapping("/periods/{periodId}/users")
    public ResponseEntity<List<UserDto>> getUsersByPeriod(@PathVariable Long periodId) {
        return ResponseEntity.ok(contributionService.getUsersContributedInPeriod(periodId));
    }

    // Lấy tất cả contributions của một user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ContributionResponseDTO>> getAllContributionsByUser(@PathVariable Long userId) {
        List<ContributionResponseDTO> contributions = contributionService.getAllContributionsByMember(userId);
        return ResponseEntity.ok(contributions);
    }

    // Lấy contributions chưa thanh toán của một user
    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<List<ContributionResponseDTO>> getPendingContributionsByUser(@PathVariable Long userId) {
        List<ContributionResponseDTO> contributions = contributionService.getPendingContributionsByMember(userId);
        return ResponseEntity.ok(contributions);
    }

    // Tạo contribution mới
    @PostMapping
    public ResponseEntity<ContributionResponseDTO> createContribution(@Valid @RequestBody ContributionDTO contributionDTO) {
        ContributionResponseDTO newContribution = contributionService.createContribution(contributionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newContribution);
    }

    // Cập nhật contribution
    @PutMapping("/{id}")
    public ResponseEntity<ContributionResponseDTO> updateContribution(
            @PathVariable Long id, @Valid @RequestBody ContributionDTO contributionDTO) {
        ContributionResponseDTO updatedContribution = contributionService.updateContribution(id, contributionDTO);
        return ResponseEntity.ok(updatedContribution);
    }

    @GetMapping("/monthly-stats")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyContributionStats(@RequestParam int year) {
        return ResponseEntity.ok(contributionService.getMonthlyContributionStats(year));
    }

    @GetMapping("/yearly-stats")
    public ResponseEntity<List<Map<String, Object>>> getYearlyContributionStats() {
        return ResponseEntity.ok(contributionService.getYearlyContributionStats());
    }
}

