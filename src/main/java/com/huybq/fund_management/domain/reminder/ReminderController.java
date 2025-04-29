package com.huybq.fund_management.domain.reminder;

import com.huybq.fund_management.domain.reminder.reminder_user.ReminderUser;
import com.huybq.fund_management.domain.reminder.reminder_user.ReminderUserResponseDTO;
import com.huybq.fund_management.domain.reminder.reminder_user.ReminderUserService;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/${server.version}/reminders")
public class ReminderController {
    private final ReminderService reminderService;
    private final ReminderUserService reminderUserService;

    @GetMapping()
    public ResponseEntity<List<ReminderResponseDTO>> getAllReminders() {
        return ResponseEntity.ok(reminderService.getAllReminders());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user")
    public ResponseEntity<List<ReminderUserResponseDTO>> getRemindersWithUserNotRead(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reminderUserService.getAllReminderWithUserId(user.getId()));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/user/{reminderId}/read")
    public ResponseEntity<?> readReminder(@PathVariable Long reminderId,@AuthenticationPrincipal User user) {
        reminderUserService.readReminder(reminderId,user.getId());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/user/read/all")
    public ResponseEntity<?> readAllReminder(@AuthenticationPrincipal User user) {
        reminderUserService.readAllReminder(user.getId());
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<Set<UserResponseDTO>> getUsersByReminderId(@PathVariable Long reminderId) {
        Set<UserResponseDTO> users = reminderService.findUsersByReminderId(reminderId);
        return ResponseEntity.ok(users);
    }

    //    @GetMapping("/user/{userId}")
//    public ResponseEntity<List<ReminderDTO>> getRemindersByUserId(@PathVariable Long userId) {
//        return ResponseEntity.ok(reminderService.getRemindersByUser(userId));
//    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public ResponseEntity<?> createReminder(@RequestBody ReminderDTO reminderDTO, @AuthenticationPrincipal User user) {
        reminderService.createReminder(reminderDTO,user.getEmail());
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
