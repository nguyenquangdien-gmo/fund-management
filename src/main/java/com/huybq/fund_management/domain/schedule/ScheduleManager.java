package com.huybq.fund_management.domain.schedule;

import com.huybq.fund_management.domain.event.EventService;
import com.huybq.fund_management.domain.late.LateService;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;


@Service
@RequiredArgsConstructor
public class ScheduleManager {
    private  final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final TaskScheduler taskScheduler;

    private final ScheduleRepository scheduleRepository;

    private final EventService eventService;

    private final LateService lateService;

    private ScheduledFuture<?> eventTask;
    private ScheduledFuture<?> lateTask;
    private ScheduledFuture<?> lateSummaryTask;


    @PostConstruct
    public void init() {
        rescheduleEventNotificationTask();
        scheduleLateTask();
        scheduleMonthlyLateSummaryTask();
    }

    public synchronized void rescheduleEventNotificationTask() {
        // Hủy task cũ nếu đang chạy
        if (eventTask != null && !eventTask.isCancelled()) {
            eventTask.cancel(false); // false = không interrupt nếu đang chạy
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
    }
    public synchronized void scheduleLateTask() {
        // Cancel task cũ nếu đang tồn tại
        if (lateTask != null && !lateTask.isCancelled()) {
            lateTask.cancel(false);
        }

        Schedule schedule = scheduleRepository.findByType(Schedule.NotificationType.LATE_NOTIFICATION)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        LocalTime sendTime = schedule.getSendTime();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

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
                lateService::fetchLateCheckins,
                Date.from(firstRun.toInstant()),
                oneDay
        );
    }

    public synchronized void scheduleMonthlyLateSummaryTask() {
        if (lateSummaryTask != null && !lateSummaryTask.isCancelled()) {
            lateSummaryTask.cancel(false);
        }

        ZonedDateTime now = ZonedDateTime.now(VIETNAM_ZONE);
        ZonedDateTime firstOfNextMonth = now.withDayOfMonth(1)
                .withHour(10).withMinute(0).withSecond(0).withNano(0);

        if (firstOfNextMonth.isBefore(now)) {
            firstOfNextMonth = firstOfNextMonth.plusMonths(1);
        }

        long oneMonth = Duration.ofDays(30).toMillis(); // safe default, không hoàn hảo nếu tháng 28/29/31

        lateSummaryTask = taskScheduler.scheduleAtFixedRate(
                lateService::sendLateReminder,
                Date.from(firstOfNextMonth.toInstant()),
                oneMonth
        );
//        ZonedDateTime now = ZonedDateTime.now(VIETNAM_ZONE);
//        ZonedDateTime firstRun = now.plusSeconds(10); // 👈 chỉ để test: chạy sau 10 giây
//
//        long oneMonth = Duration.ofSeconds(10).toMillis(); // hoặc Duration.ofMinutes(1) để test lặp lại
//
//        lateSummaryTask = taskScheduler.scheduleAtFixedRate(
//                lateService::sendLateReminder,
//                Date.from(firstRun.toInstant()),
//                oneMonth
//        );
    }

    public synchronized void updateSchedule(Schedule.NotificationType type) {
        if (type == Schedule.NotificationType.EVENT_NOTIFICATION) {
            rescheduleEventNotificationTask();
        } else if (type == Schedule.NotificationType.LATE_NOTIFICATION) {
            scheduleLateTask(); // sẽ cancel và reschedule
        }else {
            scheduleMonthlyLateSummaryTask();
        }
    }
}
