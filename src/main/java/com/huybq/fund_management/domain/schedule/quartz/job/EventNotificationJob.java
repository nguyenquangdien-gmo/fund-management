package com.huybq.fund_management.domain.schedule.quartz.job;

import com.huybq.fund_management.domain.event.EventService;
import lombok.NoArgsConstructor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class EventNotificationJob extends QuartzJobBean {
    
    @Autowired
    private EventService eventService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (eventService == null) {
            System.out.println("WARNING: eventService is null!");
            return;
        }
        eventService.sendEventNotifications();
    }
}
