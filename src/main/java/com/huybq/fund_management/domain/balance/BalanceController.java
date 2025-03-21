package com.huybq.fund_management.domain.balance;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/balances")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class BalanceController {
    private final BalanceService balanceService;

    @GetMapping
    public ResponseEntity<List<Balance>> getAllBalances() {
        return ResponseEntity.ok(balanceService.findAllBalances());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Balance> getBalanceById(@PathVariable Integer id) {
        return ResponseEntity.ok(balanceService.findBalanceById(id));
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<Balance> getBalanceByTitle(@PathVariable String title) {
        return ResponseEntity.ok(balanceService.findBalanceByTitle(title));
    }

    @PostMapping
    public ResponseEntity<Balance> createBalance(@RequestBody BalanceDTO balanceDTO) {
        Balance createdBalance = balanceService.createBalance(balanceDTO);
        return new ResponseEntity<>(createdBalance, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Balance> updateBalance(@PathVariable Integer id, @RequestBody BalanceDTO balanceDTO) {
        Balance updatedBalance = balanceService.updateBalance(id, balanceDTO);
        return ResponseEntity.ok(updatedBalance);
    }

    @PatchMapping("/deposit")
    public ResponseEntity<Void> depositBalance(@RequestParam String title, @RequestParam BigDecimal amount) {
        balanceService.depositBalance(title, amount);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/withdraw")
    public ResponseEntity<Balance> withdrawBalance(@RequestParam String title, @RequestParam BigDecimal amount) {
        Balance updatedBalance = balanceService.withdrawBalance(title, amount);
        return ResponseEntity.ok(updatedBalance);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBalance(@PathVariable Integer id) {
        balanceService.deleteBalance(id);
        return ResponseEntity.noContent().build();
    }
}
