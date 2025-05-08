package com.huybq.fund_management.domain.schedule.quartz.job;

import com.huybq.fund_management.domain.pen_bill.PenBill;
import com.huybq.fund_management.domain.pen_bill.PenBillService;
import lombok.NoArgsConstructor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class PenbillNotificationJob extends QuartzJobBean {
    @Autowired
    private PenBillService penBillService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        penBillService.sendNotificationPenBillNew();
    }
}
