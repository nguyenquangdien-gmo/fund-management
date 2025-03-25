package com.huybq.fund_management.domain.late;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/${server.version}/late")
@CrossOrigin("*")
public class LateController {
    private final LateService service;

    @GetMapping("/users")
    public ResponseEntity<List<Late>> getLateRecords(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<Late> lateRecords = service.getLateRecordsByDateRange(fromDate, toDate);
        return ResponseEntity.ok(lateRecords);
    }

    @PostMapping("/process/messages")
    public ResponseEntity<?> processLateMessage(@RequestBody String message) {
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        service.saveLateRecords(message);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/date")
    public ResponseEntity<List<LateDTO>> getLateRecordsByUserAndDate(@RequestParam int min) {
        return ResponseEntity.ok(service.getUsersWithMultipleLatesInMonth( min));
    }

}
