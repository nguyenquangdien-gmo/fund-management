package com.huybq.fund_management.domain.self_reminder;

import com.huybq.fund_management.domain.user.User;
import org.springframework.stereotype.Component;

@Component
public class SelfReminderMapper {

    public SelfReminder toEntity(SelfReminderRequestDTO dto, User user) {
        return SelfReminder.builder()
                .user(user)
                .title(dto.getTitle())
                .message(dto.getMessage())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .notifyHour(dto.getNotifyHour())
                .repeatCount(dto.getRepeatCount())
                .repeatIntervalDays(dto.getRepeatIntervalDays())
                .status(SelfReminder.ReminderStatus.ACTIVE)
                .build();
    }

    public SelfReminderResponseDTO toDto(SelfReminder entity) {
        return SelfReminderResponseDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .notifyHour(entity.getNotifyHour())
                .repeatCount(entity.getRepeatCount())
                .repeatIntervalDays(entity.getRepeatIntervalDays())
                .status(entity.getStatus().name())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public void updateEntity(SelfReminder entity, SelfReminderRequestDTO dto) {
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getMessage() != null && !dto.getMessage().isBlank()) {
            entity.setMessage(dto.getMessage());
        }
        if (dto.getStartTime() != null) {
            entity.setStartTime(dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            entity.setEndTime(dto.getEndTime());
        }
        if (dto.getNotifyHour() != null) {
            entity.setNotifyHour(dto.getNotifyHour());
        }
        if (dto.getRepeatCount() != null) {
            entity.setRepeatCount(dto.getRepeatCount());
        }
        if (dto.getRepeatIntervalDays() != null) {
            entity.setRepeatIntervalDays(dto.getRepeatIntervalDays());
        }
    }
}

