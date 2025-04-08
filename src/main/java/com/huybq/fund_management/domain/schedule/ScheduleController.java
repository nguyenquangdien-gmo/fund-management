package com.huybq.fund_management.domain.schedule;

import com.huybq.fund_management.domain.schedule.Schedule.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/${server.version}/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // CREATE
    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(@RequestBody ScheduleDTO request) {
        return ResponseEntity.ok(scheduleService.createSchedule(request));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }

    // GET BY TYPE
    @GetMapping("/type/{type}")
    public ResponseEntity<ScheduleResponse> getSchedulesByType(@PathVariable String type) {
        return ResponseEntity.ok(scheduleService.getSchedulesByType(type));
    }

    // UPDATE
    @PutMapping("/{type}")
    public ResponseEntity<ScheduleResponse> updateSchedule(@PathVariable String type,
                                                           @RequestBody ScheduleDTO request) {
        return ResponseEntity.ok(scheduleService.updateSchedule(type, request));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
