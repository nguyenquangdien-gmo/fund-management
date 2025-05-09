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

@Service
@RequiredArgsConstructor
public class QuartzScheduleManager {
    private final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final SchedulerFactoryBean schedulerFactoryBean;

    private final ScheduleRepository scheduleRepository;

    @PostConstruct
    public void initializeScheduler() {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            if (scheduler.isShutdown()) {
                scheduler.start();
            }
            scheduleAllJobs(); // Make sure this is called
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to initialize the scheduler", e);
        }
    }

    public void scheduleAllJobs() {
        try {
            // Clear all existing jobs and triggers first
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.clear();
            
            System.out.println("Cleared all existing jobs and triggers");
            
            // Now schedule all jobs
            scheduleEventNotificationJob();
            scheduleLateNotificationJob();
            scheduleContributedNotificationJob();
            scheduleCheckinJob(); // Add checkin job
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to schedule jobs", e);
        }
    }

    public void scheduleEventNotificationJob() {
        try {
            Schedule schedule = scheduleRepository.findByType(Schedule.NotificationType.EVENT_NOTIFICATION)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));

            LocalTime sendTime = schedule.getSendTime();
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            // Check if job already exists and delete it
            JobKey jobKey = JobKey.jobKey("eventNotificationJob", "notificationGroup");
            TriggerKey triggerKey = TriggerKey.triggerKey("eventNotificationTrigger", "notificationGroup");
            
            if (scheduler.checkExists(triggerKey)) {
                scheduler.unscheduleJob(triggerKey);
                System.out.println("Removed existing event notification trigger");
            }
            
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                System.out.println("Removed existing event notification job");
            }

            // Create job detail
            JobDetail jobDetail = JobBuilder.newJob(EventNotificationJob.class)
                    .withIdentity(jobKey)
                    .storeDurably()
                    .build();

            // Create trigger with daily schedule at specified time
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobDetail) // Explicitly link trigger to job
                    .startNow()
                    .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(sendTime.getHour(), sendTime.getMinute())
                            .inTimeZone(java.util.TimeZone.getTimeZone(VIETNAM_ZONE)))
                    .build();

            // Schedule the job
            scheduler.addJob(jobDetail, true);
            scheduler.scheduleJob(trigger);

            System.out.println("Event notification job scheduled at " + sendTime + " Vietnam time");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void scheduleLateNotificationJob() {
        try {
            Schedule schedule = scheduleRepository.findByType(Schedule.NotificationType.LATE_NOTIFICATION)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));

            LocalTime sendTime = schedule.getSendTime();
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            // Check if job already exists and delete it
            JobKey jobKey = JobKey.jobKey("lateNotificationJob", "notificationGroup");
            TriggerKey triggerKey = TriggerKey.triggerKey("lateNotificationTrigger", "notificationGroup");
            
            if (scheduler.checkExists(triggerKey)) {
                scheduler.unscheduleJob(triggerKey);
                System.out.println("Removed existing late notification trigger");
            }
            
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                System.out.println("Removed existing late notification job");
            }

            // Create job detail
            JobDetail jobDetail = JobBuilder.newJob(LateNotificationJob.class)
                    .withIdentity(jobKey)
                    .storeDurably()
                    .build();

            // Create trigger with daily schedule at specified time
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobDetail) // Explicitly link trigger to job
                    .startNow()
                    .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(sendTime.getHour(), sendTime.getMinute())
                            .inTimeZone(java.util.TimeZone.getTimeZone(VIETNAM_ZONE)))
                    .build();

            // Schedule the job
            scheduler.addJob(jobDetail, true);
            scheduler.scheduleJob(trigger);

            System.out.println("Late notification job scheduled at " + sendTime + " Vietnam time");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void scheduleContributedNotificationJob() {
        try {
            Schedule schedule = scheduleRepository.findByType(Schedule.NotificationType.LATE_CONTRIBUTED_NOTIFICATION)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));

            LocalTime sendTime = schedule.getSendTime();
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            // Check if job already exists and delete it
            JobKey jobKey = JobKey.jobKey("contributedNotificationJob", "notificationGroup");
            TriggerKey triggerKey = TriggerKey.triggerKey("contributedNotificationTrigger", "notificationGroup");
            
            if (scheduler.checkExists(triggerKey)) {
                scheduler.unscheduleJob(triggerKey);
                System.out.println("Removed existing contributed notification trigger");
            }
            
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                System.out.println("Removed existing contributed notification job");
            }

            // Create job detail
            JobDetail jobDetail = JobBuilder.newJob(ContributedNotificationJob.class)
                    .withIdentity(jobKey)
                    .storeDurably()
                    .build();

            // Create trigger with daily schedule at specified time
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobDetail) // Explicitly link trigger to job
                    .startNow()
                    .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(sendTime.getHour(), sendTime.getMinute())
                            .inTimeZone(java.util.TimeZone.getTimeZone(VIETNAM_ZONE)))
                    .build();

            // Schedule the job
            scheduler.addJob(jobDetail, true);
            scheduler.scheduleJob(trigger);

            System.out.println("Contributed notification job scheduled at " + sendTime + " Vietnam time");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void scheduleCheckinJob() {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            // Check if job already exists and delete it
            JobKey jobKey = JobKey.jobKey("checkinJob", "checkinGroup");
            TriggerKey triggerKey = TriggerKey.triggerKey("checkinTrigger", "checkinGroup");
            
            if (scheduler.checkExists(triggerKey)) {
                scheduler.unscheduleJob(triggerKey);
                System.out.println("Removed existing checkin trigger");
            }
            
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                System.out.println("Removed existing checkin job");
            }

            // Create job detail
            JobDetail jobDetail = JobBuilder.newJob(CheckinJob.class)
                    .withIdentity(jobKey)
                    .storeDurably()
                    .build();

            // Create trigger with daily schedule at 10:05 AM
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobDetail) // Explicitly link trigger to job
                    .startNow()
                    .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(10, 5)
                            .inTimeZone(java.util.TimeZone.getTimeZone(VIETNAM_ZONE)))
                    .build();

            // Schedule the job
            scheduler.addJob(jobDetail, true);
            scheduler.scheduleJob(trigger);

            System.out.println("Checkin job scheduled at 10:05 AM Vietnam time");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void schedulePenBillNotificationJob() {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            // Check if job already exists and delete it
            JobKey jobKey = JobKey.jobKey("penbillNotificationJob", "notificationGroup");
            TriggerKey triggerKey = TriggerKey.triggerKey("penbillNotificationTrigger", "notificationGroup");

            if (scheduler.checkExists(triggerKey)) {
                scheduler.unscheduleJob(triggerKey);
                System.out.println("Removed existing checkin trigger");
            }

            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                System.out.println("Removed existing checkin job");
            }

            // Create job detail
            JobDetail jobDetail = JobBuilder.newJob(PenbillNotificationJob.class)
                    .withIdentity(jobKey)
                    .storeDurably()
                    .build();

            // Create trigger with daily schedule at 9:00 AM
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobDetail) // Explicitly link trigger to job
                    .startNow()
                    .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(9, 0)
                            .inTimeZone(java.util.TimeZone.getTimeZone(VIETNAM_ZONE)))
                    .build();

            // Schedule the job
            scheduler.addJob(jobDetail, true);
            scheduler.scheduleJob(trigger);

            System.out.println("pen bill notification job scheduled at 9:00 AM Vietnam time");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSchedule(Schedule.NotificationType type) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            switch (type) {
                case EVENT_NOTIFICATION:
                    scheduler.unscheduleJob(TriggerKey.triggerKey("eventNotificationTrigger", "notificationGroup"));
                    scheduleEventNotificationJob();
                    break;
                case LATE_NOTIFICATION:
                    scheduler.unscheduleJob(TriggerKey.triggerKey("lateNotificationTrigger", "notificationGroup"));
                    scheduleLateNotificationJob();
                    break;
                case LATE_CONTRIBUTED_NOTIFICATION:
                    scheduler.unscheduleJob(TriggerKey.triggerKey("contributedNotificationTrigger", "notificationGroup"));
                    scheduleContributedNotificationJob();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
