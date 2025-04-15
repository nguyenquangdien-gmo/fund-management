package com.huybq.fund_management.domain.work;

import com.huybq.fund_management.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/${server.version}/works")
@RequiredArgsConstructor
public class WorkController {
    private final WorkService service;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkResponseDTO> createWorkStatus(@RequestBody WorkDTO request) {
        WorkResponseDTO response = service.createWork(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WorkResponseDTO>> getWorkStatusesByUserId(@PathVariable Long userId) {
        List<WorkResponseDTO> responses = service.getWorksByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/month/{month}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkResponseDTO>> getWorkStatusesByUserIdAndMonth(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {

        List<WorkResponseDTO> responses = service.getWorksByUserIdAndMonth(
                userId, month.getYear(), month.getMonthValue());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkResponseDTO>> getWorkStatusesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<WorkResponseDTO> responses = service.getWorksByDate(date);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkResponseDTO> approveWorkStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        WorkResponseDTO response = service.approveWork(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkResponseDTO> rejectWorkStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        WorkResponseDTO response = service.approveWork(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWorkStatus(@PathVariable Long id) {
        service.deleteWorkStatus(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}/wfh-count/{month}/type/{type}")
    public ResponseEntity<Long> getWfhCountForMonth(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @PathVariable String type
    ) {
        Long count = service.countWorkDaysInMonthWithType(userId, type.toUpperCase(), month.getYear(), month.getMonthValue());
        return ResponseEntity.ok(count);
    }

}
