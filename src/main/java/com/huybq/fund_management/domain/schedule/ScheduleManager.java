package com.huybq.fund_management.domain.schedule;

import com.huybq.fund_management.domain.contributions.ContributionService;
import com.huybq.fund_management.domain.event.EventService;
import com.huybq.fund_management.domain.late.LateService;
import com.huybq.fund_management.domain.pen_bill.PenBillService;
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

    private final ContributionService contributionService;

    private final TeamRepository teamRepository;
    private final PenBillService penBillService;

    private ScheduledFuture<?> eventTask;
    private ScheduledFuture<?> lateTask;
    private ScheduledFuture<?> contributedTask;

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
        ZonedDateTime firstRun = now.withHour(sendTime.getHour()).withMinute(sendTime.getMinute()).withSecond(30);

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

    public synchronized void rescheduleContributedNotificationTask() {
        // Hủy task cũ nếu đang chạy
        if (contributedTask != null && !contributedTask.isCancelled()) {
            contributedTask.cancel(false);
        }

        Schedule schedule = scheduleRepository.findByType(Schedule.NotificationType.LATE_CONTRIBUTED_NOTIFICATION)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with type: LATE_CONTRIBUTED_NOTIFICATION"));

        LocalDate fromDate = schedule.getFromDate().toLocalDate();
        LocalDate toDate = schedule.getToDate().toLocalDate();
        LocalTime sendTime = schedule.getSendTime();

        ZonedDateTime now = ZonedDateTime.now(VIETNAM_ZONE);
        ZonedDateTime scheduledTime = now.withHour(sendTime.getHour())
                .withMinute(sendTime.getMinute())
                .withSecond(0)
                .withNano(0);

        // Nếu thời gian đã qua trong ngày hôm nay, lên lịch cho ngày mai
        if (scheduledTime.isBefore(now)) {
            scheduledTime = scheduledTime.plusDays(1);
        }

        long initialDelay = Duration.between(now, scheduledTime).toMillis();
        long oneDay = Duration.ofDays(1).toMillis();

        System.out.println("[ContributedTask] Now: " + now);
        System.out.println("[ContributedTask] Scheduled time: " + scheduledTime);
        System.out.println("[ContributedTask] Initial delay (ms): " + initialDelay);
        System.out.println("[ContributedTask] Valid date range: " + fromDate + " to " + toDate);

        contributedTask = taskScheduler.scheduleAtFixedRate(
                () -> {
                    LocalDate today = LocalDate.now(VIETNAM_ZONE);
                    System.out.println("[ContributedTask] Today's date: " + today);

                    boolean isWithinDateRange = (today.isEqual(fromDate) || today.isAfter(fromDate)) &&
                            (today.isEqual(toDate) || today.isBefore(toDate));

                    if (isWithinDateRange) {
                        System.out.println("[ContributedTask] Today (" + today + ") is within the configured date range. Sending notification...");
                        contributionService.sendUnpaidCheckinBillNotification();
                    } else {
                        System.out.println("[ContributedTask] Today (" + today + ") is NOT within date range from " + fromDate + " to " + toDate + ". Skipping notification.");
                    }
                },
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
        ZonedDateTime now = ZonedDateTime.now(VIETNAM_ZONE);

//         Tính thời gian chạy đầu tiên trong ngày
        ZonedDateTime firstRun = now.withHour(sendTime.getHour())
                .withMinute(sendTime.getMinute())
                .withSecond(sendTime.getSecond())
                .withNano(0);

        if (firstRun.isBefore(now)) {
            firstRun = firstRun.plusDays(1);
        }

        long oneDay = Duration.ofDays(1).toMillis();

        lateTask = taskScheduler.scheduleAtFixedRate(
                penBillService::sendUnpaidCheckinBillNotification,
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
//                    penBillService.sendUnpaidCheckinBillNotification();
//                },
//                new Date(System.currentTimeMillis() + initialDelay),
//                repeatInterval
//        );

    }

    public synchronized void updateSchedule(Schedule.NotificationType type) {
        if (type == Schedule.NotificationType.EVENT_NOTIFICATION) {
            rescheduleEventNotificationTask();
        } else if (type == Schedule.NotificationType.LATE_NOTIFICATION) {
            scheduleLateTask(); // sẽ cancel và reschedule
        }
        else if (type == Schedule.NotificationType.LATE_CONTRIBUTED_NOTIFICATION) {
            rescheduleContributedNotificationTask(); // sẽ cancel và reschedule
        }
//        else {
//            scheduleMonthlyLateSummaryTask();
//        }
    }
}
