package com.huybq.fund_management.domain.fund;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/funds")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FundController {
    private final FundService fundService;

    @PostMapping
    public ResponseEntity<Fund> createFund(@RequestBody FundDTO fundDTO) {
        return ResponseEntity.ok(fundService.createFund(fundDTO));
    }

    @GetMapping
    public ResponseEntity<List<Fund>> getAllFunds() {
        return ResponseEntity.ok(fundService.getAllFunds());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fund> getFundById(@PathVariable Integer id) {
        return ResponseEntity.ok(fundService.getFundById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Fund> updateFund(@PathVariable Integer id, @RequestBody @Valid FundDTO fundDTO) {
        return ResponseEntity.ok(fundService.updateFund(id, fundDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFund(@PathVariable Integer id) {
        fundService.deleteFund(id);
        return ResponseEntity.noContent().build();
    }
}
