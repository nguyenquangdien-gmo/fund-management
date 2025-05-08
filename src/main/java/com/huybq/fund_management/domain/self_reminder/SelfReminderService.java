package com.huybq.fund_management.domain.self_reminder;

import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SelfReminderService {

    private final SelfReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final SelfReminderMapper mapper;

    private final int MAX_REMINDERS_PER_USER = 10;

    public SelfReminderResponseDTO createReminder(Long userId, SelfReminderRequestDTO request) {
        long count = reminderRepository.countByUserId(userId);
        if (count >= MAX_REMINDERS_PER_USER) {
            throw new IllegalStateException("Bạn đã đạt giới hạn nhắc nhở.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        SelfReminder reminder = mapper.toEntity(request, user);
        return mapper.toDto(reminderRepository.save(reminder));
    }

    public List<SelfReminderResponseDTO> getRemindersByUser(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<SelfReminder> reminders;

        if (startDate != null && endDate != null) {
            reminders = reminderRepository.findByUserIdAndCreatedAtBetween(userId, startDate, endDate);
        } else if (startDate != null) {
            reminders = reminderRepository.findByUserIdAndCreatedAtAfter(userId, startDate);
        } else if (endDate != null) {
            reminders = reminderRepository.findByUserIdAndCreatedAtBefore(userId, endDate);
        } else {
            reminders = reminderRepository.findByUserId(userId);
        }

        return reminders.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public SelfReminderResponseDTO updateReminder(Long reminderId, Long userId, SelfReminderRequestDTO request) {
        SelfReminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Reminder không tồn tại"));

        if (!reminder.getUser().getId().equals(userId)) {
            throw new SecurityException("Không có quyền sửa reminder này.");
        }

        mapper.updateEntity(reminder, request);
        return mapper.toDto(reminderRepository.save(reminder));
    }

    public SelfReminderResponseDTO deleteReminder(Long reminderId, Long userId) {
        SelfReminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Reminder không tồn tại"));

        if (!reminder.getUser().getId().equals(userId)) {
            throw new SecurityException("Không có quyền xóa reminder này.");
        }

        reminder.setStatus(SelfReminder.ReminderStatus.valueOf("DISABLED"));
        return mapper.toDto(reminderRepository.save(reminder));

//        reminderRepository.delete(reminder);
    }
}
