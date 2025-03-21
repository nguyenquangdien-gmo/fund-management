package com.huybq.fund_management.domain.reminder;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reminders")
public class ReminderController {
    private final ReminderService reminderService;

    @GetMapping()
    public ResponseEntity<List<ReminderDTO>> getAllReminders() {
        return ResponseEntity.ok(reminderService.getAllUniqueReminders());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReminderDTO>> getRemindersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(reminderService.getRemindersByUser(userId));
    }
    @PostMapping("create/other")
    public ResponseEntity<?> createOtherReminder(@RequestBody ReminderDTO reminderDTO) {
        reminderService.createOtherReminder(reminderDTO);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create/monthly")
    public ResponseEntity<?> createMonthlyReminders(@RequestParam int month, @RequestParam int year) {
        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest().body("Invalid month: " + month);
        }
        if (year > LocalDate.now().getYear()) {
            return ResponseEntity.badRequest().body("Invalid year: " + year);
        }

        reminderService.createMonthlyReminders(month, year);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create/not-contributed")
    public ResponseEntity<?> createRemindersNotYetContributed(@RequestParam int month, @RequestParam int year) {
        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest().body("Invalid month: " + month);
        }
        if (year > LocalDate.now().getYear()) {
            return ResponseEntity.badRequest().body("Invalid year: " + year);
        }

        reminderService.createRemindersForUserNotContributionOrOwed(month, year);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/mark-read/{reminderId}")
    public ResponseEntity<?> updateReminder(@PathVariable Long reminderId) {
        reminderService.markAsRead(reminderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/mark-reads")
    public ResponseEntity<String> markAsReadMulti(@RequestBody List<Long> reminderIds) {
        reminderService.markAsReadMulti(reminderIds);
        return ResponseEntity.ok("Reminders marked as read successfully.");
    }

}
