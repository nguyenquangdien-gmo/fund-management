package com.huybq.fund_management.domain.event;

import com.huybq.fund_management.domain.schedule.Schedule;
import com.huybq.fund_management.domain.schedule.ScheduleRepository;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import com.huybq.fund_management.utils.chatops.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final Notification notification;
    private final EventMapper mapper;

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


//        @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Ho_Chi_Minh") // Chạy mỗi ngày 08:00 sáng
//    @Scheduled(cron = "*/10 * * * * ?", zone = "Asia/Ho_Chi_Minh")
//@Scheduled(cron = "0 0 * * * *", zone = "Asia/Ho_Chi_Minh") // hoặc đặt cron phù hợp
//public void sendEventNotifications() {
//    LocalDateTime now = LocalDateTime.now(VIETNAM_ZONE);
//
//    Schedule schedule = scheduleRepository.findByType(Schedule.NotificationType.EVENT_NOTIFICATION)
//            .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
//
//    List<Event> events = eventRepository.findByEventTimeBetween(schedule.getFromDate(), schedule.getToDate());
//
//    for (Event event : events) {
//        LocalDateTime eventTime = event.getEventTime();
//
//        // if thời điểm hiện tại là sendTime của ngày hôm nay
//        LocalTime sendTime = schedule.getSendTime();
//        LocalDateTime todaySendTime = LocalDateTime.of(now.toLocalDate(), sendTime);
//
//        if (now.isAfter(todaySendTime.minusMinutes(1)) && now.isBefore(todaySendTime.plusMinutes(1))) {
//            // Kiểm tra sự kiện diễn ra sau 1 hoặc 2 ngày nữa
//            if (eventTime.toLocalDate().equals(now.toLocalDate().plusDays(1)) ||
//                    eventTime.toLocalDate().equals(now.toLocalDate().plusDays(2))) {
//
//                notification.sendNotification("📢 Nhắc lịch: Sự kiện " + event.getName() +
//                        "\nSẽ diễn ra vào " + eventTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
//                        "\nTại " + event.getLocation() + "\nChủ sự là: " + event.getHosts(), "java");
//            }
//        }
//
//        // 3. Thông báo trước 1 giờ diễn ra sự kiện
//        Duration duration = Duration.between(now, eventTime);
//        if (!duration.isNegative() && duration.toMinutes() <= 60 && duration.toMinutes() >= 59) {
//            notification.sendNotification("🚀 Sự kiện " + event.getName() +
//                    " sắp diễn ra trong 1 giờ tại " + event.getLocation(), "java");
//        }
//    }
//}

    public void sendEventNotifications() {
        LocalDateTime now = LocalDateTime.now(VIETNAM_ZONE);

        Schedule schedule = scheduleRepository.findByType(Schedule.NotificationType.EVENT_NOTIFICATION)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        // Dùng phương thức có @EntityGraph
        List<Event> events = eventRepository.findByEventTimeBetween(schedule.getFromDate(), schedule.getToDate());
        if (events.isEmpty()) {
            return;
        }

        StringBuilder notificationMessage = new StringBuilder();
        LocalDate today = now.toLocalDate();
        List<String> todayEvents = new ArrayList<>();
        List<String> upcomingEvents = new ArrayList<>();

        for (Event event : events) {
            LocalDateTime eventTime = event.getEventTime();
            String hosts = event.getHosts().stream()
                    .map(user -> "@" + user.getEmail().replace("@", "-"))
                    .collect(Collectors.joining(", "));

            String eventInfo = "- " + event.getName() +
                    " vào " + eventTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                    " tại " + event.getLocation() +
                    " (Chủ sự: " + hosts + ")";

            if (eventTime.toLocalDate().equals(today)) {
                todayEvents.add(eventInfo);
            } else {
                upcomingEvents.add(eventInfo);
            }
        }

        if (!todayEvents.isEmpty()) {
            notificationMessage.append("📢 Các sự kiện diễn ra vào ngày hôm nay:\n");
            todayEvents.forEach(info -> notificationMessage.append(info).append("\n"));
        }

        if (!upcomingEvents.isEmpty()) {
            if (!todayEvents.isEmpty()) {
                notificationMessage.append("\n"); // Nếu có cả hôm nay và sắp tới thì cách dòng
            }
            notificationMessage.append("📢 Các sự kiện sắp diễn ra:\n");
            upcomingEvents.forEach(info -> notificationMessage.append(info).append("\n"));
        }

        if (!notificationMessage.isEmpty()) {
            notification.sendNotification(notificationMessage.toString(), "java");
        }
    }


    public void sendNowNotifications(Long idEvent) {
        Event event = eventRepository.findById(idEvent)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        String hosts = event.getHosts().stream()
                .map(user -> "\n @" + user.getEmail().replace("@", "-"))
                .collect(Collectors.joining("\n"));
        notification.sendNotification("@all\n \uD83D\uDCE2 Nhắc lịch: Sự kiện " + event.getName() +
                "\nSẽ diễn ra vào: " + event.getEventTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                "\nTại: " + event.getLocation() + "\nChủ sự là: " + hosts, "java");
    }


    @Scheduled(cron = "0 * * * * *", zone = "Asia/Ho_Chi_Minh")
    public void sendOneHourBeforeNotifications() {
        LocalDateTime now = LocalDateTime.now(VIETNAM_ZONE);

        List<Event> events = eventRepository.findAll();
        for (Event event : events) {
            LocalDateTime eventTime = event.getEventTime();
            Duration duration = Duration.between(now, eventTime);
            if (!duration.isNegative() && duration.toMinutes() <= 60 && duration.toMinutes() >= 59) {
                notification.sendNotification("\uD83D\uDE80 Sự kiện " + event.getName() +
                        " sắp diễn ra trong 1 giờ tại " + event.getLocation(), "java");
            }
        }
    }


    public List<EventResponeseDTO> getAllEvents() {
        return eventRepository.findAllByEventTimeGreaterThanEqual(LocalDateTime.now()).stream()
                .map(mapper::toResponseDTO).toList();
    }

    public EventResponeseDTO getEventById(Long id) {
        return mapper.toResponseDTO(eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found")));

    }

    public EventResponeseDTO updateEvent(Long id, EventDTO eventDTO) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        existingEvent.setName(eventDTO.getName());
        existingEvent.setEventTime(eventDTO.getEventTime());
        existingEvent.setLocation(eventDTO.getLocation());

        // Update hosts
        List<User> hosts = userRepository.findAllById(eventDTO.getHostIds());
        existingEvent.setHosts(hosts);

        return mapper.toResponseDTO(eventRepository.save(existingEvent));
    }

    public void deleteEvent(Long id) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        eventRepository.delete(existingEvent);
    }

    // Additional methods for specific queries
    public List<EventResponeseDTO> searchEventsByName(String name) {
        return eventRepository.findByNameContaining(name).stream()
                .map(mapper::toResponseDTO).toList();
    }

    public List<EventResponeseDTO> getEventsByDateRange(LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByEventTimeBetween(start, end).stream()
                .map(mapper::toResponseDTO).toList();
    }
}
