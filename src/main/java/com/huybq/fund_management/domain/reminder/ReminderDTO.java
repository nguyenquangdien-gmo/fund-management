package com.huybq.fund_management.domain.reminder;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ReminderDTO(
        String title,
        String description,
        String type,
        LocalDateTime scheduledTime,
        boolean isSendChatGroup,
        List<Long> userIds,
        String emailException
) {

}
