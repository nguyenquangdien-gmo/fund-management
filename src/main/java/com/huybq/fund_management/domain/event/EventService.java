package com.huybq.fund_management.domain.event;

import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public Event createEvent(EventDTO eventDTO) {
        Event event = new Event();
        event.setName(eventDTO.getName());
        event.setEventTime(eventDTO.getEventTime());
        event.setLocation(eventDTO.getLocation());

        // Find and set hosts
        List<User> hosts = userRepository.findAllById(eventDTO.getHostIds());
        event.setHosts(hosts);

        return eventRepository.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    public Event updateEvent(Long id, EventDTO eventDTO) {
        Event existingEvent = getEventById(id);

        existingEvent.setName(eventDTO.getName());
        existingEvent.setEventTime(eventDTO.getEventTime());
        existingEvent.setLocation(eventDTO.getLocation());

        // Update hosts
        List<User> hosts = userRepository.findAllById(eventDTO.getHostIds());
        existingEvent.setHosts(hosts);

        return eventRepository.save(existingEvent);
    }

    public void deleteEvent(Long id) {
        Event event = getEventById(id);
        eventRepository.delete(event);
    }

    // Additional methods for specific queries
    public List<Event> searchEventsByName(String name) {
        return eventRepository.findByNameContaining(name);
    }

    public List<Event> getEventsByDateRange(LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByEventTimeBetween(start, end);
    }
}
