package com.huybq.fund_management.domain.work;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/${server.version}/works")
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorkResponseDTO>> getAllWorks() {
        List<WorkResponseDTO> response = workService.getAllWorks();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorkResponseDTO>> getWorksByUserId(@PathVariable Long userId) {
        List<WorkResponseDTO> response = workService.getWorksByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorkResponseDTO>> createWork(@RequestBody WorkDTO request) {
        List<WorkResponseDTO> createdWorks = workService.createWork(request);
        return new ResponseEntity<>(createdWorks, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateWork(@PathVariable Long id, @RequestBody WorkUpdateDTO request) {
        workService.updateWork(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{date}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserWorkResponse>> getWorksByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<UserWorkResponse> statuses = workService.getWorksByDate(date);
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/user/{userId}/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countWorkDaysInMonth(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam String type) {
        Long count = workService.countDaysInMonthWithType(userId, year, month,type.toUpperCase());
        return ResponseEntity.ok(count);
    }

    @GetMapping("/work-summary")
    public ResponseEntity<List<WorkSummaryResponse>> getWorkSummaryByMonth(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(workService.getWorkSummaryByMonth(year, month));
    }

    @GetMapping("/user/{userId}/details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorkResponseDTO>> getUserWorkDetails(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        List<WorkResponseDTO> details = workService.getUserWorkDetails(userId, year, month);
        return ResponseEntity.ok(details);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWork(@PathVariable Long id) {
        workService.deleteWork(id);
        return ResponseEntity.noContent().build();
    }
}
