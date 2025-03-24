package com.huybq.fund_management.domain.user.controller;

import com.huybq.fund_management.domain.user.dto.UserDebtDTO;
import com.huybq.fund_management.domain.user.dto.UserDto;
import com.huybq.fund_management.domain.user.dto.UserLatePaymentDTO;
import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @GetMapping("/debt-or-no-contribution/period")
    public List<UserDebtDTO> getUsersWithDebtOrNoContribution(
            @RequestParam int month,
            @RequestParam int year) {
        return userService.getUsersWithDebtOrNoContribution(month, year);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long userId, @RequestBody UserDto userDtouser) {
        return ResponseEntity.ok(userService.updateUserById(userId, userDtouser));
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
