package com.huybq.fund_management.domain.user.controller;

import com.huybq.fund_management.domain.reminder.Reminder;
import com.huybq.fund_management.domain.user.dto.UserDebtDTO;
import com.huybq.fund_management.domain.user.dto.UserDto;
import com.huybq.fund_management.domain.user.dto.UserLatePaymentDTO;
import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.service.UserService;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @GetMapping("/{userId}/reminders")
    public ResponseEntity<List<Reminder>> getRemindersByUserId(@PathVariable Long userId) {
        List<Reminder> reminders = userService.findRemindersByUserId(userId);
        return ResponseEntity.ok(reminders);
    }
    @PostMapping("/get-user")
    public ResponseEntity<UserDto> getUserByEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email"); // Lấy email từ body request
        UserDto user = userService.getUserByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/no-contribution/period")
    public List<UserDebtDTO> getUsersWithNoContribution(
            @RequestParam int month,
            @RequestParam int year) {
        return userService.getUsersWithNoContribution(month, year);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long userId, @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.updateUserById(userId, userDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(userService.deleteUserById(userId));
    }

    @GetMapping("/late-payments")
    public ResponseEntity<List<UserLatePaymentDTO>> getLatePayments(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(userService.getLatePayments(month, year));
    }


}
