package com.huybq.fund_management.domain.self_reminder;

import com.huybq.fund_management.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "self_reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String title;
    private String message;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private LocalTime notifyHour;
    private Integer repeatCount;
    private Integer repeatIntervalDays;

    @Enumerated(EnumType.STRING)
    private ReminderStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
        if (status == null) status = ReminderStatus.ACTIVE;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum ReminderStatus {
        ACTIVE, DISABLED, EXPIRED
    }
}


