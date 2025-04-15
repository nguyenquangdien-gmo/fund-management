package com.huybq.fund_management.domain.reminder;

import com.huybq.fund_management.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/${server.version}/reminders")
public class ReminderController {
    private final ReminderService reminderService;

    @GetMapping()
    public ResponseEntity<List<ReminderResponseDTO>> getAllReminders() {
        return ResponseEntity.ok(reminderService.getAllReminders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReminderResponseDTO> getReminderById(@PathVariable Long id) {
        return ResponseEntity.ok(reminderService.getReminderById(id));
    }

    @GetMapping("/{reminderId}/user/completed")
    public ResponseEntity<Boolean> hasUserCompletedSurvey(@PathVariable Long reminderId,
                                                          @AuthenticationPrincipal User user) {
        boolean completed = reminderService.hasUserCompletedSurvey(reminderId, user.getId());
        return ResponseEntity.ok(completed);
    }

    @GetMapping("/{reminderId}/users")
    public ResponseEntity<Set<User>> getUsersByReminderId(@PathVariable Long reminderId) {
        Set<User> users = reminderService.findUsersByReminderId(reminderId);
        return ResponseEntity.ok(users);
    }

    //    @GetMapping("/user/{userId}")
//    public ResponseEntity<List<ReminderDTO>> getRemindersByUserId(@PathVariable Long userId) {
//        return ResponseEntity.ok(reminderService.getRemindersByUser(userId));
//    }
    @PostMapping("/create")
    public ResponseEntity<?> createReminder(@RequestBody ReminderDTO reminderDTO) {
        reminderService.createReminder(reminderDTO);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReminder(@PathVariable Long id, @RequestBody ReminderDTO reminderDTO) {
        reminderService.updateReminder(id, reminderDTO);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("{reminderId}/survey/completed")
    public ResponseEntity<?> completeSurvey(@PathVariable Long reminderId, @AuthenticationPrincipal User user) {
        reminderService.completeSurvey(reminderId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{reminderId}/survey/status")
    public ResponseEntity<List<SurveyStatusDTO>> getSurveyStatus(@PathVariable Long reminderId) {
        return ResponseEntity.ok(reminderService.getSurveyResults(reminderId));
    }
//
//    @GetMapping("/survey/users")
//    public ResponseEntity<?> getUsersNotYetContributed(@RequestParam Long reminderId) {
//        return ResponseEntity.ok(reminderService.getSurveyCompletionStats(reminderId));
//    }

//    @PostMapping("/create/monthly")
//    public ResponseEntity<?> createMonthlyReminders(@RequestParam int month, @RequestParam int year) {
//        if (month < 1 || month > 12) {
//            return ResponseEntity.badRequest().body("Invalid month: " + month);
//        }
//        if (year > LocalDate.now().getYear()) {
//            return ResponseEntity.badRequest().body("Invalid year: " + year);
//        }
//
//        reminderService.createMonthlyReminders(month, year);
//        return ResponseEntity.noContent().build();
//    }
//
//    @PostMapping("/create/not-contributed")
//    public ResponseEntity<?> createRemindersNotYetContributed(@RequestParam int month, @RequestParam int year) {
//        if (month < 1 || month > 12) {
//            return ResponseEntity.badRequest().body("Invalid month: " + month);
//        }
//        if (year > LocalDate.now().getYear()) {
//            return ResponseEntity.badRequest().body("Invalid year: " + year);
//        }
//
//        reminderService.createRemindersForUserNotContributionOrOwed(month, year);
//        return ResponseEntity.noContent().build();
//    }


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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReminder(@PathVariable Long id) {
        reminderService.deleteReminder(id);
        return ResponseEntity.noContent().build();
    }
}
