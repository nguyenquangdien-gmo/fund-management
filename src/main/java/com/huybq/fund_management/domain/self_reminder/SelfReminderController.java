package com.huybq.fund_management.domain.self_reminder;

import com.huybq.fund_management.domain.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/${server.version}/self-reminders")
@RequiredArgsConstructor
public class SelfReminderController {

    private final SelfReminderService reminderService;

    // Tạo reminder
    @PostMapping
    public ResponseEntity<SelfReminderResponseDTO> create(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid SelfReminderRequestDTO request
    ) {
        return ResponseEntity.ok(reminderService.createReminder(user.getId(), request));
    }

    // Danh sách reminder của user
    @GetMapping
    public ResponseEntity<List<SelfReminderResponseDTO>> getAllByUser(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return ResponseEntity.ok(reminderService.getRemindersByUser(user.getId(), startDate, endDate));
    }


    // Sửa reminder
    @PatchMapping("/{id}")
    public ResponseEntity<SelfReminderResponseDTO> update(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid SelfReminderRequestDTO request
    ) {
        return ResponseEntity.ok(reminderService.updateReminder(id, user.getId(), request));
    }

    // Xóa reminder
    @DeleteMapping("/{id}")
    public ResponseEntity<SelfReminderResponseDTO> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        reminderService.deleteReminder(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}

