package com.huybq.fund_management.domain.reminder.reminder_user;

import com.huybq.fund_management.domain.reminder.Reminder;
import com.huybq.fund_management.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminder_user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReminderUser {

    @EmbeddedId
    private ReminderUserId id = new ReminderUserId();

    @ManyToOne
    @MapsId("reminderId")
    @JoinColumn(name = "reminder_id")
    private Reminder reminder;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Reminder.Status status = Reminder.Status.SENT;

    private boolean completed = false;

    private LocalDateTime finishedAt;

    public ReminderUser(Reminder reminder, User user) {
        this.reminder = reminder;
        this.user = user;
        this.id = new ReminderUserId(reminder.getId(), user.getId());
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            this.id = new ReminderUserId(reminder.getId(), user.getId());
        }
    }
}
