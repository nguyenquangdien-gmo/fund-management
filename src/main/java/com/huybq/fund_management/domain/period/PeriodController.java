package com.huybq.fund_management.domain.period;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/periods")
@RequiredArgsConstructor
public class PeriodController {
    private final PeriodService periodService;

    // Lấy tất cả Periods
    @GetMapping
    public ResponseEntity<List<Period>> getAllPeriods() {
        return ResponseEntity.ok(periodService.getAllPeriods());
    }

    // Lấy Period theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Period> getPeriodById(@PathVariable Long id) {
        return ResponseEntity.ok(periodService.getPeriodById(id));
    }

    // Tạo mới Period
    @PostMapping
    public ResponseEntity<Period> createPeriod(@Valid @RequestBody Period period) {
        Period createdPeriod = periodService.createPeriod(period);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPeriod);
    }

    // Cập nhật Period
    @PutMapping("/{id}")
    public ResponseEntity<Period> updatePeriod(@PathVariable Long id, @Valid @RequestBody Period updatedPeriod) {
        Period updated = periodService.updatePeriod(id, updatedPeriod);
        return ResponseEntity.ok(updated);
    }

    // Xóa Period
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePeriod(@PathVariable Long id) {
        periodService.deletePeriod(id);
        return ResponseEntity.noContent().build();
    }
}

