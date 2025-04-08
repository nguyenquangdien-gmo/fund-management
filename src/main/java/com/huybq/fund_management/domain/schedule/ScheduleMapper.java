package com.huybq.fund_management.domain.schedule;

import org.springframework.stereotype.Service;

@Service
public class ScheduleMapper {
    public ScheduleResponse toResponse(Schedule schedule) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .fromDate(schedule.getFromDate())
                .toDate(schedule.getToDate())
                .sendTime(schedule.getSendTime())
                .type(schedule.getType())
                .build();
    }
    public Schedule toEntity(ScheduleDTO scheduleDTO) {
        return Schedule.builder()
                .fromDate(scheduleDTO.getFromDate())
                .toDate(scheduleDTO.getToDate())
                .sendTime(scheduleDTO.getSendTime())
                .type(Schedule.NotificationType.valueOf(scheduleDTO.getType().toUpperCase()))
                .build();
    }
}
