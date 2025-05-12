package com.huybq.fund_management.domain.schedule;

import com.huybq.fund_management.domain.schedule.quartz.manager.QuartzScheduleManager;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper mapper;
    private final ScheduleManager scheduleManager;

    public ScheduleResponse getSchedulesByType(String type) {
        Schedule schedule = scheduleRepository.findByType(Schedule.NotificationType.valueOf(type.toUpperCase())).orElseThrow(() -> new ResourceNotFoundException("Schedule not found with type: " + type));
        return mapper.toResponse(schedule);
    }


    public ScheduleResponse createSchedule(ScheduleDTO request) {
        Schedule schedule = mapper.toEntity(request);
        try {
            Schedule saved = scheduleRepository.save(schedule);
            return mapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Schedule with type " + request.getType() + " already exists.");
        }
    }

    public List<ScheduleResponse> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public ScheduleResponse updateSchedule(String type, ScheduleDTO request) {
        Schedule schedule = scheduleRepository.findByType(Schedule.NotificationType.valueOf(type))
                .orElseThrow(()-> new ResourceNotFoundException("Schedule not found with type: " + type));

        schedule.setFromDate(request.getFromDate());
        schedule.setToDate(request.getToDate());
        schedule.setSendTime(request.getSendTime());

        if (request.getChannelId() != null && !request.getChannelId().isEmpty()) {
            schedule.setChannelId(request.getChannelId());
        }

        try {
            Schedule updated = scheduleRepository.save(schedule);
            scheduleManager.updateSchedule(Schedule.NotificationType.valueOf(type));
            return mapper.toResponse(updated);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Schedule with type " + request.getType() + " already exists.");
        }
    }


    public void deleteSchedule(Long id) {
        Schedule schedule = findByIdOrThrow(id);
        scheduleRepository.delete(schedule);
    }

    private Schedule findByIdOrThrow(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id " + id));
    }
}
