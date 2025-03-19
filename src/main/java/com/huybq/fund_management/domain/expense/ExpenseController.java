package com.huybq.fund_management.domain.expense;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getExpenses());
    }

    @GetMapping("/total-amount")
    public ResponseEntity<BigDecimal> getTotalAmount() {
        return ResponseEntity.ok(expenseService.getTotalAmount());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByMonthAndYear(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(expenseService.getExpensesByMonthAndYear(month, year));
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalAmountByMonthAndYear(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(expenseService.getTotalAmountByMonthAndYear(month, year));
    }
    @GetMapping("/total-year")
    public ResponseEntity<BigDecimal> getTotalAmountByYear(
            @RequestParam int year) {
        return ResponseEntity.ok(expenseService.getTotalAmountByYear(year));
    }

    @PostMapping
    public ResponseEntity<ExpenseDTO> createExpense(@Valid @RequestBody ExpenseDTO dto) {
        return ResponseEntity.ok(expenseService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseDTO dto) {
        return ResponseEntity.ok(expenseService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
