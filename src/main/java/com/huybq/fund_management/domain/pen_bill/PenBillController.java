package com.huybq.fund_management.domain.pen_bill;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/${server.version}/pen-bills")
@RequiredArgsConstructor
public class PenBillController {
    private final PenBillService penBillService;

    @GetMapping
    public ResponseEntity<List<PenBillDTO>> getAllPenBills() {
        return ResponseEntity.ok(penBillService.getAllPenBills());
    }
    @GetMapping("/pending")
    public ResponseEntity<List<PenBillDTO>> getPenBillsPending() {
        return ResponseEntity.ok(penBillService.getPenBillsPending());
    }

    @GetMapping("/user/{userId}/unpaid")
    public ResponseEntity<List<PenBillDTO>> getBillsUnPaidByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(penBillService.getAllBillsUnPaidByUserId(userId));
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PenBillDTO>> getBillsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(penBillService.getAllBillsByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PenBillDTO> getPenBillById(@PathVariable Long id) {
        return ResponseEntity.ok(penBillService.getPenBillById(id));
    }

    @PostMapping
    public ResponseEntity<Void> createPenBill(@Valid @RequestBody PenBillDTO penBillDTO) {
        penBillService.createPenBill(penBillDTO);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<PenBillDTO> updatePenBill(@PathVariable Long id) {
        return ResponseEntity.ok(penBillService.updatePenBill(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePenBill(@PathVariable Long id) {
        penBillService.deletePenBill(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approvePenBill(@PathVariable Long id) {
        penBillService.approvePenBill(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectPenBill(@PathVariable Long id) {
        penBillService.rejectPenBill(id);
        return ResponseEntity.noContent().build();
    }
}
