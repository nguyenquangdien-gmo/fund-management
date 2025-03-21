package com.huybq.fund_management.domain.penalty;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/penalties")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PenaltyController {
    private final PenaltyService penaltyService;

    @GetMapping
    public ResponseEntity<List<PenaltyDTO>> getAllPenalties() {
        return ResponseEntity.ok(penaltyService.getAllPenalties());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PenaltyDTO> getPenaltyById(@PathVariable Long id) {
        return ResponseEntity.ok(penaltyService.getPenaltyById(id));
    }

    @PostMapping
    public ResponseEntity<PenaltyDTO> createPenalty(@Valid @RequestBody PenaltyDTO penaltyDTO) {
        return ResponseEntity.ok(penaltyService.createPenalty(penaltyDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PenaltyDTO> updatePenalty(@PathVariable Long id, @Valid @RequestBody PenaltyDTO updatedDTO) {
        return ResponseEntity.ok(penaltyService.updatePenalty(id, updatedDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePenalty(@PathVariable Long id) {
        penaltyService.deletePenalty(id);
        return ResponseEntity.noContent().build();
    }
}
