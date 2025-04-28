package com.huybq.fund_management.domain.reminder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huybq.fund_management.domain.reminder.reminder_user.ReminderUser;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToMany(mappedBy = "reminder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ReminderUser> reminderUsers = new ArrayList<>();

    private String title;
    private String description;
//    private BigDecimal owedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderType reminderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.SENT;

    private LocalDateTime scheduledTime;

    private boolean isSendChatGroup;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime lastSentDate;


    public enum ReminderType {
        CONTRIBUTION, PENALTY, OTHER, SURVEY
    }

    public enum Status {
        UNSENT,SENT, READ,FINISHED
    }
}
