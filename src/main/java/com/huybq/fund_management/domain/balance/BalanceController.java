package com.huybq.fund_management.domain.balance;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/${server.version}/balances")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class BalanceController {
    private final BalanceService balanceService;

    @GetMapping
    public ResponseEntity<?> getAllBalances() {
        try {
            return ResponseEntity.ok(balanceService.findAllBalances());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve balances: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBalanceById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(balanceService.findBalanceById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Balance not found: " + e.getMessage());
        }
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<?> getBalanceByTitle(@PathVariable String title) {
        try {
            return ResponseEntity.ok(balanceService.findBalanceByTitle(title));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Balance not found: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createBalance(@RequestBody BalanceDTO balanceDTO) {
        try {
            Balance createdBalance = balanceService.createBalance(balanceDTO);
            return new ResponseEntity<>(createdBalance, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create balance: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBalance(@PathVariable Integer id, @RequestBody BalanceDTO balanceDTO) {
        try {
            return ResponseEntity.ok(balanceService.updateBalance(id, balanceDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to update balance: " + e.getMessage());
        }
    }

    @PatchMapping("/deposit")
    public ResponseEntity<?> depositBalance(@RequestParam String title, @RequestParam BigDecimal amount) {
        try {
            balanceService.depositBalance(title, amount);
            return ResponseEntity.ok("Deposit successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to deposit: " + e.getMessage());
        }
    }

    @PatchMapping("/withdraw")
    public ResponseEntity<?> withdrawBalance(@RequestParam String title, @RequestParam BigDecimal amount) {
        try {
            return ResponseEntity.ok(balanceService.withdrawBalance(title, amount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to withdraw: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBalance(@PathVariable Integer id) {
        try {
            balanceService.deleteBalance(id);
            return ResponseEntity.ok("Balance deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to delete balance: " + e.getMessage());
        }
    }
}
