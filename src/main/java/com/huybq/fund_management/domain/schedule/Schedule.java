package com.huybq.fund_management.domain.schedule;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    private LocalTime sendTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,unique = true)
    private NotificationType type;
    private String channelId;


    public enum NotificationType {
        LATE_NOTIFICATION, EVENT_NOTIFICATION, LATE_CONTRIBUTED_NOTIFICATION
    }
}
