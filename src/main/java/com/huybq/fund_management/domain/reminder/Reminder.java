package com.huybq.fund_management.domain.reminder;

import com.huybq.fund_management.domain.contributions.Contribution;
import com.huybq.fund_management.domain.penalty.Penalty;
import com.huybq.fund_management.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderType reminderType;

    @ManyToOne
    @JoinColumn(name = "contribution_id")
    private Contribution contribution;

    @ManyToOne
    @JoinColumn(name = "penalty_id")
    private Penalty penalty;

    @Column(nullable = false)
    private LocalDate sentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.SENT;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    public enum ReminderType {
        CONTRIBUTION, PENALTY
    }


    public enum Status {
        SENT, READ, IGNORED
    }
}
