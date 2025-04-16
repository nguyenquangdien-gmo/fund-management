package com.huybq.fund_management.domain.reminder.reminder_user;

import com.huybq.fund_management.domain.reminder.Reminder;
import com.huybq.fund_management.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Builder
@AllArgsConstructor
@Data
public class ReminderUserResponseDTO {
    private Reminder reminder;

    private Long userId;

    private String status;

    private boolean completed = false;

    private LocalDateTime finishedAt;

}
