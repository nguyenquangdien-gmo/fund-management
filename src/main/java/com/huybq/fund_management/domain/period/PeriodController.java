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

    @GetMapping
    public ResponseEntity<List<PeriodDTO>> getAllPeriods() {
        return ResponseEntity.ok(periodService.getAllPeriods());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PeriodDTO> getPeriodById(@PathVariable Long id) {
        return ResponseEntity.ok(periodService.getPeriodById(id));
    }

    @PostMapping
    public ResponseEntity<PeriodDTO> createPeriod(@Valid @RequestBody PeriodDTO periodDTO) {
        PeriodDTO createdPeriod = periodService.createPeriod(periodDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPeriod);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PeriodDTO> updatePeriod(@PathVariable Long id, @Valid @RequestBody PeriodDTO updatedPeriod) {
        PeriodDTO updated = periodService.updatePeriod(id, updatedPeriod);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePeriod(@PathVariable Long id) {
        periodService.deletePeriod(id);
        return ResponseEntity.noContent().build();
    }
}
