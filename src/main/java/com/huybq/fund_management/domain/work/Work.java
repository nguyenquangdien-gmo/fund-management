package com.huybq.fund_management.domain.work;

import com.huybq.fund_management.domain.user.Status;
import com.huybq.fund_management.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
    public class Work {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        @Column(nullable = false)
        private LocalDate fromDate;

        @Column(nullable = false)
        private LocalDate toDate;

        @Enumerated(EnumType.STRING)
        private StatusType type; // WFH, LEAVE

        @Enumerated(EnumType.STRING)
        private TimePeriod timePeriod; // AM, PM, FULL

        private String reason;

        @Enumerated(EnumType.STRING)
        private Status status;

        @ManyToOne
        @JoinColumn(name = "approved_by")
        private User approvedBy;

        @CreationTimestamp
        private LocalDateTime createdAt;

        public enum Status {
            PENDING, APPROVED, REJECTED
        }
    }

