package com.huybq.fund_management.domain.reminder.reminder_user;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class ReminderUserId implements Serializable {

    private Long reminderId;

    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReminderUserId)) return false;
        ReminderUserId that = (ReminderUserId) o;
        return Objects.equals(reminderId, that.reminderId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reminderId, userId);
    }
}

