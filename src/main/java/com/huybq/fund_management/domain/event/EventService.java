package com.huybq.fund_management.domain.event;

import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import com.huybq.fund_management.utils.chatops.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final Notification notification;

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


        @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Ho_Chi_Minh") // Chạy mỗi ngày 08:00 sáng
//    @Scheduled(cron = "*/10 * * * * ?", zone = "Asia/Ho_Chi_Minh")
    public void sendEventNotifications() {
        LocalDateTime now = LocalDateTime.now(VIETNAM_ZONE);

        List<Event> events = eventRepository.findAll();

        for (Event event : events) {
            LocalDateTime eventTime = event.getEventTime(); // Lấy thời gian sự kiện

            // Kiểm tra nếu hôm nay là 2 ngày trước sự kiện
            if (now.toLocalDate().equals(eventTime.minusDays(2).toLocalDate()) || now.toLocalDate().equals(eventTime.minusDays(1).toLocalDate())) {
                notification.sendNotification("📢 Nhắc lịch: Sự kiện " + event.getName() +
                        " sẽ diễn ra vào " + eventTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                        " tại " + event.getLocation(), "java");
            }

            // Kiểm tra nếu thời gian hiện tại là 1 giờ trước sự kiện
            if (now.isAfter(eventTime.minusHours(1)) && now.isBefore(eventTime)) {
                notification.sendNotification("🚀 Sự kiện " + event.getName() +
                        " sắp diễn ra trong 1 giờ tại " + event.getLocation(), "java");
            }
        }
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
