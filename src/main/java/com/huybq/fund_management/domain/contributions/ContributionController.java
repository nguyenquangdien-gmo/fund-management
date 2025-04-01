package com.huybq.fund_management.domain.contributions;

import com.huybq.fund_management.domain.user.dto.UserDto;
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
    public ResponseEntity<?> getAllContributions() {
        try {
            return ResponseEntity.ok(contributionService.getAllContributions());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve contributions: " + e.getMessage());
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getContributionById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(contributionService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contribution not found: " + e.getMessage());
        }
    }

    @GetMapping("/owed/users")
    public ResponseEntity<?> getUsersNotContributed(@RequestParam int month, @RequestParam int year) {
        try {
            return ResponseEntity.ok(contributionService.getUsersOwedContributed(month, year));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve users: " + e.getMessage());
        }
    }

    @GetMapping("/")
    public ResponseEntity<?> getContributionByMonthAndYear(@RequestParam int month, @RequestParam int year) {
        try {
            return ResponseEntity.ok(contributionService.getAllContributionsByMonthAndYear(month, year));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve contributions: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createContribution(@Valid @RequestBody ContributionDTO contributionDTO) {
        try {
            ContributionResponseDTO newContribution = contributionService.createContribution(contributionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(newContribution);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create contribution: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateContribution(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(contributionService.updateContribution(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to update contribution: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveContribution(@PathVariable Long id) {
        try {
            contributionService.approveContribution(id);
            return ResponseEntity.ok("Contribution approved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to approve contribution: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectContribution(@PathVariable Long id) {
        try {
            contributionService.rejectContribution(id);
            return ResponseEntity.ok("Contribution rejected or update canceled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to reject contribution: " + e.getMessage());
        }
    }
}
