package com.huybq.fund_management.domain.contributions;

import com.huybq.fund_management.domain.user.UserDTO;
import com.huybq.fund_management.domain.user.UserResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/${server.version}/contributions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContributionController {

    private final ContributionService contributionService;

    @GetMapping
    public ResponseEntity<List<ContributionResponseDTO>> getAllContributions() {
        return ResponseEntity.ok(contributionService.getAllContributions());
    }

    @GetMapping("{id}")
    public ResponseEntity<ContributionResponseDTO> getContributionById(@PathVariable Long id) {
        return ResponseEntity.ok(contributionService.findById(id));
    }
    @GetMapping("/owed/users")
    public ResponseEntity<List<UserResponseDTO>> getUsersNotContributed(@RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(contributionService.getUsersOwedContributed(month, year));
    }

    @GetMapping("/")
    public ResponseEntity<List<ContributionResponseDTO>> getContributionById(@RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(contributionService.getAllContributionsByMonthAndYear(month, year));
    }

    @PostMapping("/confirm/dept-contribution")
    public ResponseEntity<List<ContributionResponseDTO>> createAndApproveContributions(
            @Valid @RequestBody ContributionDTO contributionDTO) {
        contributionService.confirmContributionWithDeptContribution(contributionDTO);
        return ResponseEntity.noContent().build();
    }

    // Lấy tất cả contributions của một kỳ (period)
    @GetMapping("/period/{periodId}")
    public ResponseEntity<List<ContributionResponseDTO>> getAllContributionsByPeriod(@PathVariable Long periodId) {
        List<ContributionResponseDTO> contributions = contributionService.findAllContributions(periodId);
        return ResponseEntity.ok(contributions);
    }

    @GetMapping("/periods/{periodId}/users")
    public ResponseEntity<List<UserResponseDTO>> getUsersByPeriod(@PathVariable Long periodId) {
        return ResponseEntity.ok(contributionService.getUsersContributedInPeriod(periodId));
    }

    // Lấy tất cả contributions của một user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ContributionResponseDTO>> getAllContributionsByUser(@PathVariable Long userId) {
        List<ContributionResponseDTO> contributions = contributionService.getAllContributionsByMember(userId);
        return ResponseEntity.ok(contributions);
    }

    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<List<ContributionResponseDTO>> getPendingContributionsByUser(@PathVariable Long userId) {
        List<ContributionResponseDTO> contributions = contributionService.getPendingContributionsByMember(userId);
        return ResponseEntity.ok(contributions);
    }

    @PostMapping
    public ResponseEntity<ContributionResponseDTO> createContribution(@Valid @RequestBody ContributionDTO contributionDTO) {
        ContributionResponseDTO newContribution = contributionService.createContribution(contributionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newContribution);
    }
    // Cập nhật contribution
    @PutMapping("/{id}")
    public ResponseEntity<ContributionResponseDTO> updateContribution(
            @PathVariable Long id) {
        ContributionResponseDTO updatedContribution = contributionService.updateContribution(id);
        return ResponseEntity.ok(updatedContribution);
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getPaidAmountContributed(@RequestParam int year) {
        return ResponseEntity.ok(contributionService.getTotalContributionAmountByPeriod(year));
    }

    @GetMapping("/monthly-stats")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyContributionStats(@RequestParam int year) {
        return ResponseEntity.ok(contributionService.getMonthlyContributionStats(year));
    }

    @GetMapping("{year}/stats")
    public ResponseEntity<?> getYearlyContributionStats(@PathVariable int year) {
        return ResponseEntity.ok(contributionService.getYearContributionStats(year));
    }

    //approve when having request creation or update
    @PostMapping("/{id}/approve")
    public ResponseEntity<String> approveContribution(@PathVariable Long id) {
        contributionService.approveContribution(id);
        return ResponseEntity.ok("Contribution approved successfully");
    }

    //reject when wrong result
    @PostMapping("/{id}/reject")
    public ResponseEntity<String> rejectContribution(@PathVariable Long id,@RequestBody Map<String,String>request) {
//        System.out.println("request: "+request);
        contributionService.rejectContribution(id, request.get("reason"));
        return ResponseEntity.ok("Contribution rejected or update canceled successfully");
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ContributionResponseDTO>> getPendingContributions() {
        List<ContributionResponseDTO> pendingContributions = contributionService.getPendingContributions();
        return ResponseEntity.ok(pendingContributions);
    }
}

