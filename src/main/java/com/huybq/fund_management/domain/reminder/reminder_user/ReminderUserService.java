package com.huybq.fund_management.domain.reminder.reminder_user;

import com.huybq.fund_management.domain.reminder.Reminder;
import com.huybq.fund_management.domain.reminder.ReminderResponseDTO;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderUserService {
    private final ReminderUserRepository reminderUserRepository;

    public List<ReminderUserResponseDTO> getAllReminderWithUserId(Long userId) {
        return reminderUserRepository.findByUserIdOrderByReminderCreatedAtDesc(userId).stream()
                .map(reminderUser -> ReminderUserResponseDTO.builder()
                        .userId(reminderUser.getUser().getId())
                        .reminder(reminderUser.getReminder())
                        .status(reminderUser.getStatus().name())
                        .completed(reminderUser.isCompleted())
                        .finishedAt(reminderUser.getFinishedAt())
                        .build()).collect(Collectors.toList());
    }

    public void readReminder(Long reminderId,Long userId) {
        ReminderUser reminderUser = reminderUserRepository.findReminderUserByReminder_IdAndUser_Id(reminderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with ID: " + reminderId));
        reminderUser.setStatus(Reminder.Status.READ);
        reminderUserRepository.save(reminderUser);
    }

    @Transactional
    public void readAllReminder(Long userId) {
        reminderUserRepository.findByUserIdOrderByReminderCreatedAtDesc(userId)
                .forEach(ru -> {
                    ru.setStatus(Reminder.Status.READ);
                    reminderUserRepository.save(ru);
                });
    }
}
