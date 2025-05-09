package com.huybq.fund_management.domain.schedule.quartz.job;

import com.huybq.fund_management.utils.chatops.Notification;
import lombok.NoArgsConstructor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class BirthdayAndAnniversaryNotificationsJob extends QuartzJobBean {
    @Autowired
    private Notification notification;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        notification.sendBirthdayAndAnniversaryNotifications();
    }
}
