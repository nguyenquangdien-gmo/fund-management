package com.huybq.fund_management.domain.schedule.quartz.manager;

import com.huybq.fund_management.domain.schedule.Schedule;
import com.huybq.fund_management.domain.schedule.ScheduleRepository;
import com.huybq.fund_management.domain.schedule.quartz.job.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.TimeZone;

@Service
@RequiredArgsConstructor
public class QuartzScheduleManager {
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final TimeZone VIETNAM_TIMEZONE = TimeZone.getTimeZone(VIETNAM_ZONE);

    private final SchedulerFactoryBean schedulerFactoryBean;
    private final ScheduleRepository scheduleRepository;

    @PostConstruct
    public void initializeScheduler() {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            if (scheduler.isShutdown()) {
                scheduler.start();
            }
            scheduleAllJobs();
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to initialize the scheduler", e);
        }
    }

    public void scheduleAllJobs() {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.clear();

            System.out.println("Cleared all existing jobs and triggers");

            // Static-time jobs
            scheduleJobWithFixedTime("BirthdayAndAnniversaryNotificationsJob", "BirthdayAndAnniversaryNotificationsTrigger", "notificationGroup",
                    BirthdayAndAnniversaryNotificationsJob.class, 9, 0);
            scheduleJobWithFixedTime("checkinJob", "checkinTrigger", "checkinGroup",
                    CheckinJob.class, 10, 5);
            scheduleJobWithFixedTime("penbillNotificationJob", "penbillNotificationTrigger", "notificationGroup",
                    PenbillNotificationJob.class, 9, 0);

            // Dynamic-time jobs
            scheduleJobWithDynamicTime(Schedule.NotificationType.EVENT_NOTIFICATION,
                    "eventNotificationJob", "eventNotificationTrigger", "notificationGroup",
                    EventNotificationJob.class);
            scheduleJobWithDynamicTime(Schedule.NotificationType.LATE_NOTIFICATION,
                    "lateNotificationJob", "lateNotificationTrigger", "notificationGroup",
                    LateNotificationJob.class);
            scheduleJobWithDynamicTime(Schedule.NotificationType.LATE_CONTRIBUTED_NOTIFICATION,
                    "contributedNotificationJob", "contributedNotificationTrigger", "notificationGroup",
                    ContributedNotificationJob.class);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to schedule jobs", e);
        }
    }

    private void scheduleJobWithFixedTime(String jobName, String triggerName, String group,
                                          Class<? extends Job> jobClass, int hour, int minute) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            JobKey jobKey = JobKey.jobKey(jobName, group);
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, group);

            removeExistingJob(scheduler, jobKey, triggerKey);

            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jobKey)
                    .storeDurably()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobDetail)
                    .startNow()
                    .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(hour, minute)
                            .inTimeZone(VIETNAM_TIMEZONE))
                    .build();

            scheduler.addJob(jobDetail, true);
            scheduler.scheduleJob(trigger);

            System.out.printf("%s scheduled at %02d:%02d Vietnam time%n", jobName, hour, minute);

        } catch (Exception e) {
            System.err.printf("Failed to schedule %s: %s%n", jobName, e.getMessage());
            e.printStackTrace();
        }
    }

    private void scheduleJobWithDynamicTime(Schedule.NotificationType type, String jobName,
                                            String triggerName, String group, Class<? extends Job> jobClass) {
        try {
            Schedule schedule = scheduleRepository.findByType(type)
                    .orElseThrow(() -> new RuntimeException("Schedule not found for type: " + type));

            LocalTime sendTime = schedule.getSendTime();

            scheduleJobWithFixedTime(jobName, triggerName, group, jobClass, sendTime.getHour(), sendTime.getMinute());

        } catch (Exception e) {
            System.err.printf("Failed to schedule job for type %s: %s%n", type, e.getMessage());
            e.printStackTrace();
        }
    }

    private void removeExistingJob(Scheduler scheduler, JobKey jobKey, TriggerKey triggerKey) throws SchedulerException {
        if (scheduler.checkExists(triggerKey)) {
            scheduler.unscheduleJob(triggerKey);
            System.out.printf("Removed existing trigger: %s%n", triggerKey.getName());
        }

        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
            System.out.printf("Removed existing job: %s%n", jobKey.getName());
        }
    }

    public void updateSchedule(Schedule.NotificationType type) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            switch (type) {
                case EVENT_NOTIFICATION -> {
                    scheduler.unscheduleJob(TriggerKey.triggerKey("eventNotificationTrigger", "notificationGroup"));
                    scheduleJobWithDynamicTime(type, "eventNotificationJob", "eventNotificationTrigger", "notificationGroup", EventNotificationJob.class);
                }
                case LATE_NOTIFICATION -> {
                    scheduler.unscheduleJob(TriggerKey.triggerKey("lateNotificationTrigger", "notificationGroup"));
                    scheduleJobWithDynamicTime(type, "lateNotificationJob", "lateNotificationTrigger", "notificationGroup", LateNotificationJob.class);
                }
                case LATE_CONTRIBUTED_NOTIFICATION -> {
                    scheduler.unscheduleJob(TriggerKey.triggerKey("contributedNotificationTrigger", "notificationGroup"));
                    scheduleJobWithDynamicTime(type, "contributedNotificationJob", "contributedNotificationTrigger", "notificationGroup", ContributedNotificationJob.class);
                }
                default -> throw new IllegalArgumentException("Unsupported notification type: " + type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
