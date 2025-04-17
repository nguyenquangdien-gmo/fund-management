package com.huybq.fund_management.domain.schedule;

import com.huybq.fund_management.domain.event.EventService;
import com.huybq.fund_management.domain.late.LateService;
import com.huybq.fund_management.domain.team.Team;
import com.huybq.fund_management.domain.team.TeamRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;


@Service
@RequiredArgsConstructor
public class ScheduleManager {
    private final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final TaskScheduler taskScheduler;

    private final ScheduleRepository scheduleRepository;

    private final EventService eventService;

    private final LateService lateService;

    private final TeamRepository teamRepository;

    private ScheduledFuture<?> eventTask;
    private ScheduledFuture<?> lateTask;

//    @PostConstruct
//    public void init() {
//        rescheduleEventNotificationTask();
//        scheduleLateTask();

    /// /        scheduleMonthlyLateSummaryTask();
//    }

    public synchronized void rescheduleEventNotificationTask() {
        // Hủy task cũ nếu đang chạy
        if (eventTask != null && !eventTask.isCancelled()) {
            eventTask.cancel(false);
        }

        Schedule schedule = scheduleRepository.findByType(Schedule.NotificationType.EVENT_NOTIFICATION)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        LocalTime sendTime = schedule.getSendTime();
        ZonedDateTime now = ZonedDateTime.now(VIETNAM_ZONE);
        ZonedDateTime firstRun = now.withHour(sendTime.getHour()).withMinute(sendTime.getMinute()).withSecond(0);

        if (firstRun.isBefore(now)) {
            firstRun = firstRun.plusDays(1);
        }

        long initialDelay = Duration.between(now, firstRun).toMillis();
        long oneDay = Duration.ofDays(1).toMillis();

        eventTask = taskScheduler.scheduleAtFixedRate(
                eventService::sendEventNotifications,
                new Date(System.currentTimeMillis() + initialDelay),
                oneDay
        );
        // DEBUG: set chạy sau 5 giây
//        long initialDelay = 5 * 1000L; // 5 giây
//        long repeatInterval = 10 * 1000L; // lặp lại mỗi 10 giây (cho dễ test)
//
//        System.out.println("[EventTask] Scheduled to start in 5s, repeat every 10s");
//
//        eventTask = taskScheduler.scheduleAtFixedRate(
//                () -> {
//                    System.out.println("[EventTask] Running at " + ZonedDateTime.now(VIETNAM_ZONE));
//                    eventService.sendEventNotifications();
//                },
//                new Date(System.currentTimeMillis() + initialDelay),
//                repeatInterval
//        );
    }

    public synchronized void scheduleLateTask() {
        // Cancel task cũ nếu đang tồn tại
        if (lateTask != null && !lateTask.isCancelled()) {
            lateTask.cancel(false);
        }

        Schedule schedule = scheduleRepository.findByType(Schedule.NotificationType.LATE_NOTIFICATION)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        LocalTime sendTime = schedule.getSendTime();
        ZonedDateTime now = ZonedDateTime.now(VIETNAM_ZONE);

        // Tính thời gian chạy đầu tiên trong ngày
        ZonedDateTime firstRun = now.withHour(sendTime.getHour())
                .withMinute(sendTime.getMinute())
                .withSecond(sendTime.getSecond())
                .withNano(0);

        if (firstRun.isBefore(now)) {
            firstRun = firstRun.plusDays(1);
        }

        long oneDay = Duration.ofDays(1).toMillis();

        lateTask = taskScheduler.scheduleAtFixedRate(
                () -> lateService.fetchLateCheckins(schedule.getSendTime(),schedule.getChannelId()),
                Date.from(firstRun.toInstant()),
                oneDay
        );
//        long initialDelay = 5 * 1000L; // 5 giây
//        long repeatInterval = 10 * 1000L; // lặp lại mỗi 10 giây (cho dễ test)
//
//        System.out.println("[EventTask] Scheduled to start in 5s, repeat every 10s");
//
//        lateTask = taskScheduler.scheduleAtFixedRate(
//                () -> {
//                    System.out.println("[EventTask] Running at " + ZonedDateTime.now(VIETNAM_ZONE));
//                    lateService.fetchLateCheckins(schedule.getSendTime());
//                },
//                new Date(System.currentTimeMillis() + initialDelay),
//                repeatInterval
//        );

    }

//    public synchronized void scheduleMonthlyLateSummaryTask() {
//        if (lateSummaryTask != null && !lateSummaryTask.isCancelled()) {
//            lateSummaryTask.cancel(false);
//        }
//
//        Schedule schedule = scheduleRepository.findByType(Schedule.NotificationType.LATE_NOTIFICATION)
//                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
//
//        LocalDateTime fromDate = schedule.getFromDate(); // ví dụ: 2024-04-31
//        LocalTime sendTime = schedule.getSendTime(); // ví dụ: 10:00
//        ZonedDateTime now = ZonedDateTime.now(VIETNAM_ZONE);
//
//        int configuredDay = fromDate.getDayOfMonth();
//        int maxDayOfThisMonth = now.toLocalDate().lengthOfMonth();
//
//        int safeDay = Math.min(configuredDay, maxDayOfThisMonth); // ví dụ: 31 vs 30 => 30
//
//        ZonedDateTime firstRun = now.withDayOfMonth(safeDay)
//                .withHour(sendTime.getHour())
//                .withMinute(sendTime.getMinute())
//                .withSecond(0)
//                .withNano(0);
//
//        if (firstRun.isBefore(now)) {
//            // Tháng sau
//            ZonedDateTime nextMonth = now.plusMonths(1);
//            int maxDayOfNextMonth = nextMonth.toLocalDate().lengthOfMonth();
//            int safeNextDay = Math.min(configuredDay, maxDayOfNextMonth);
//
//            firstRun = nextMonth.withDayOfMonth(safeNextDay)
//                    .withHour(sendTime.getHour())
//                    .withMinute(sendTime.getMinute())
//                    .withSecond(0)
//                    .withNano(0);
//        }
//
//        long oneMonth = Duration.ofDays(30).toMillis(); // đơn giản, ổn cho now
//
//        lateSummaryTask = taskScheduler.scheduleAtFixedRate(
//                lateService::sendLateReminder,
//                Date.from(firstRun.toInstant()),
//                oneMonth
//        );
//    }


    public synchronized void updateSchedule(Schedule.NotificationType type) {
        if (type == Schedule.NotificationType.EVENT_NOTIFICATION) {
            rescheduleEventNotificationTask();
        } else if (type == Schedule.NotificationType.LATE_NOTIFICATION) {
            scheduleLateTask(); // sẽ cancel và reschedule
        }
//        else {
//            scheduleMonthlyLateSummaryTask();
//        }
    }
}
