package com.huybq.fund_management.domain.invoice;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/${server.version}/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService service;

    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices() {
        return ResponseEntity.ok(service.getInvoices());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesWithStatusPending() {
        return ResponseEntity.ok(service.getInvoicesWithStatusPending());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoicesByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getInvoicesByUserId(userId));
    }

    @GetMapping("/total-amount")
    public ResponseEntity<BigDecimal> getTotalAmount(@RequestParam String type) {
        return ResponseEntity.ok(service.getTotalAmount(type));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByMonthAndYear(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam String type) {
        return ResponseEntity.ok(service.getInvoiceByMonthAndYear(type,month, year));
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalAmountByMonthAndYear(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam String type) {
        return ResponseEntity.ok(service.getTotalAmountByMonthAndYear(month, year,type));
    }
    @GetMapping("/total-year")
    public ResponseEntity<BigDecimal> getTotalAmountByYear(
            @RequestParam int year,
            @RequestParam String type) {
        return ResponseEntity.ok(service.getTotalAmountByYear(year,type));
    }

    @GetMapping("/monthly-stats")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyInvoiceStats(
            @RequestParam int year,
            @RequestParam String type
    ) {
        return ResponseEntity.ok(service.getMonthlyInvoiceStats(year, type));
    }

    @GetMapping("{year}/stats")
    public ResponseEntity<?> getYearlyInvoiceStats(
            @PathVariable int year,
            @RequestParam String type
    ) {
        return ResponseEntity.ok(service.getYearInvoiceStats(year,type));
    }

    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@Valid @RequestBody InvoiceDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<InvoiceResponseDTO> approveInvoice(@PathVariable Long id, @RequestParam String fundType) {
        return ResponseEntity.ok(service.approve(id, fundType));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<InvoiceResponseDTO> rejectInvoice(@PathVariable Long id,@RequestBody Map<String,String> request) {
        return ResponseEntity.ok(service.reject(id,request.get("reason")));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<InvoiceResponseDTO> updateInvoice(@PathVariable Long id, @Valid @RequestBody InvoiceDTO dto) {
        return ResponseEntity.ok(service.update(id,dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
