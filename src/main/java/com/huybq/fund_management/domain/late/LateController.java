package com.huybq.fund_management.domain.late;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<LateReponseDTO>> getLateRecordsByUserId(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<LateReponseDTO> lateRecords = service.getLateByUserIdWithDateRange(userId, fromDate, toDate);
        return ResponseEntity.ok(lateRecords);
    }

    @PostMapping("/check-now")
    public ResponseEntity<?> checkLateNow(@RequestBody Map<String,String> time) {
        service.fetchLateCheckins(LocalTime.parse(time.get("time")));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/process/messages")
    public ResponseEntity<?> processLateMessage(@RequestBody MessageRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        System.out.println("Received message: " + request.getMessage());
        service.saveLateRecords(request.getMessage());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/date")
    public ResponseEntity<List<LateDTO>> getLateRecordsByUserAndDate(@RequestParam int min) {
        return ResponseEntity.ok(service.getUsersWithMultipleLatesInMonth(min));
    }

    @GetMapping("/users/monthly")
    public List<?> getLatesFromPrevious28thToCurrent28th() {
        return service.getLatesFromPrevious1stToCurrent1st();
    }

}
