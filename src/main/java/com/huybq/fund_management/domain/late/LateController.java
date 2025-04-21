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
    public ResponseEntity<List<LateWithPenBillDTO>> getLateRecords(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<LateWithPenBillDTO> lateRecords = service.getLateRecordsWithPenBill(fromDate, toDate);
        return ResponseEntity.ok(lateRecords);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<LateResponseDTO>> getLateRecordsByUserId(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<LateResponseDTO> lateRecords = service.getLateByUserIdWithDateRange(userId, fromDate, toDate);
        return ResponseEntity.ok(lateRecords);
    }

    @PostMapping("/check-now")
    public ResponseEntity<?> checkLateNow(@RequestBody Map<String,String> schedule) {
        System.out.println("Received schedule: " + schedule.get("channelId"));
        service.fetchLateCheckins(LocalTime.parse(schedule.get("time")),schedule.get("channelId"));
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
    public ResponseEntity<List<LateDTO>> getLateRecordsByUserAndDate() {
        return ResponseEntity.ok(service.getUsersWithMultipleLatesInMonth());
    }

//    @GetMapping("/users/monthly")
//    public List<?> getLatesFromPrevious28thToCurrent28th() {
//        return service.getLatesFromPrevious1stToCurrent1st();
//    }

}
