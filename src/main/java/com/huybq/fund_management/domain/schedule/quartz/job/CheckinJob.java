package com.huybq.fund_management.domain.schedule.quartz.job;

import com.huybq.fund_management.domain.late.LateService;
import lombok.NoArgsConstructor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class CheckinJob extends QuartzJobBean {
    @Autowired
    private LateService lateService;


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        lateService.scheduledCheckinLate();
    }
}
