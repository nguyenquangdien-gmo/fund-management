package com.huybq.fund_management.domain.reminder;

import lombok.Builder;

@Builder
public record ReminderDTO(
        Long id,
        String title,
        String description,
        String type,
        String status,
        String createdAt
) {
}
