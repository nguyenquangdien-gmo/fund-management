package com.huybq.fund_management.domain.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleStartupRunner {
    private final ScheduleManager scheduleManager;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        scheduleManager.rescheduleEventNotificationTask();
        scheduleManager.scheduleLateTask();
        scheduleManager.rescheduleContributedNotificationTask();
    }
}
