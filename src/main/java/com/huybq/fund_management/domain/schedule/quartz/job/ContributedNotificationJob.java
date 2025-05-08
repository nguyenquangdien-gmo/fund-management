package com.huybq.fund_management.domain.schedule.quartz.job;

import com.huybq.fund_management.domain.contributions.ContributionService;
import com.huybq.fund_management.domain.schedule.Schedule;
import com.huybq.fund_management.domain.schedule.ScheduleRepository;
import lombok.NoArgsConstructor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@NoArgsConstructor
public class ContributedNotificationJob extends QuartzJobBean {
    
    @Autowired
    private ContributionService contributionService;

    @Autowired
    private ScheduleRepository scheduleRepository;

    private final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Schedule schedule = scheduleRepository.findByType(Schedule.NotificationType.LATE_CONTRIBUTED_NOTIFICATION)
                .orElseThrow(() -> new RuntimeException("Schedule not found with type: LATE_CONTRIBUTED_NOTIFICATION"));

        LocalDate fromDate = schedule.getFromDate().toLocalDate();
        LocalDate toDate = schedule.getToDate().toLocalDate();
        LocalDate today = LocalDate.now(VIETNAM_ZONE);

        boolean isWithinDateRange = (today.isEqual(fromDate) || today.isAfter(fromDate)) &&
                (today.isEqual(toDate) || today.isBefore(toDate));

        if (isWithinDateRange) {
            System.out.println("[ContributedTask] Today (" + today + ") is within the configured date range. Sending notification...");
            contributionService.sendUnpaidCheckinBillNotification();
        } else {
            System.out.println("[ContributedTask] Today (" + today + ") is NOT within date range from " + fromDate + " to " + toDate + ". Skipping notification.");
        }
    }
}
