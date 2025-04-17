package com.huybq.fund_management.domain.reminder;

import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserResponseDTO;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
@Builder
public record ReminderResponseDTO(
        Long id,
        String title,
        String description,
        String type,
        String status,
        String createdAt,
        LocalDateTime scheduledTime,
        boolean isSendChatGroup,
        List<UserResponseDTO> users
) {
}
