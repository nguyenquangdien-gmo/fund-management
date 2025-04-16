package com.huybq.fund_management.domain.user;

import com.huybq.fund_management.domain.reminder.Reminder;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @GetMapping("/exclude-current")
    public ResponseEntity<List<UserResponseDTO>> getUsersExcludeCurrent() {
        return ResponseEntity.ok(userService.getUsersExcludeCurrent());
    }

    @PostMapping("/get-user")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email"); // Lấy email từ body request
        UserResponseDTO user = userService.getUserByEmail(email);
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
    public ResponseEntity<?> updateUser(@PathVariable("id") Long userId, @RequestBody UserDTO userDto) {
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
