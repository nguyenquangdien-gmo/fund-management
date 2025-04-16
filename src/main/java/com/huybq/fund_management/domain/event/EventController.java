package com.huybq.fund_management.domain.event;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/${server.version}/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody EventDTO eventDTO) {
        Event createdEvent = eventService.createEvent(eventDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @GetMapping
    public ResponseEntity<List<EventResponeseDTO>> getAllEvents() {
        List<EventResponeseDTO> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponeseDTO> getEventById(@PathVariable Long id) {
        EventResponeseDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/send-now")
    public ResponseEntity<?> sendNow(@RequestBody Map<String,Long> id) {
        eventService.sendNowNotifications(id.get("id"));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponeseDTO> updateEvent(
            @PathVariable Long id,
            @RequestBody EventDTO eventDTO
    ) {
        EventResponeseDTO updatedEvent = eventService.updateEvent(id, eventDTO);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // Additional search endpoints
    @GetMapping("/search")
    public ResponseEntity<List<EventResponeseDTO>> searchEvents(@RequestParam String name) {
        List<EventResponeseDTO> events = eventService.searchEventsByName(name);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<EventResponeseDTO>> getEventsByDateRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {
        List<EventResponeseDTO> events = eventService.getEventsByDateRange(start, end);
        return ResponseEntity.ok(events);
    }
}


