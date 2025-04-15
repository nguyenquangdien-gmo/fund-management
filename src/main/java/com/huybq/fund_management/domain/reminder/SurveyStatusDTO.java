package com.huybq.fund_management.domain.reminder;

import java.time.LocalDateTime;

public record SurveyStatusDTO(
        Long userId,
        String fullName,
        boolean completed,
        LocalDateTime finishedAt
) {}

