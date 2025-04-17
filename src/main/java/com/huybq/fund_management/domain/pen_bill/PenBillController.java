package com.huybq.fund_management.domain.pen_bill;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/${server.version}/pen-bills")
@RequiredArgsConstructor
public class PenBillController {
    private final PenBillService penBillService;

    @GetMapping
    public ResponseEntity<List<PenBillResponse>> getAllPenBills() {
        return ResponseEntity.ok(penBillService.getAllPenBills());
    }
    @GetMapping("/pending")
    public ResponseEntity<List<PenBillResponse>> getPenBillsPending() {
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
        penBillService.createBill(penBillDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
    public ResponseEntity<Void> rejectPenBill(@PathVariable Long id, @RequestBody Map<String, String> request) {
        penBillService.rejectPenBill(id,request.get("reason"));
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/monthly-stats")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyPenaltyStats(@RequestParam int year) {
        return ResponseEntity.ok(penBillService.getMonthlyPenaltyStats(year));
    }

    // Tổng số tiền phạt đã thanh toán trong một năm
    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalPaidPenalties(@RequestParam int year) {
        return ResponseEntity.ok(penBillService.getTotalPaidPenaltiesByYear(year));
    }

    // Thống kê tổng tiền phạt theo từng năm
    @GetMapping("{year}/stats")
    public ResponseEntity<BillStatisticsDTO> getYearlyPenaltyStats(@PathVariable int year) {
        return ResponseEntity.ok(penBillService.getPenaltyStatsByYear(year));
    }

}
