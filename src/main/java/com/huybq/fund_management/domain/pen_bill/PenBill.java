package com.huybq.fund_management.domain.pen_bill;

import com.huybq.fund_management.domain.penalty.Penalty;
import com.huybq.fund_management.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenBill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "penalty_id", nullable = false)
    private Penalty penalty;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status paymentStatus = Status.PENDING;

    @Column(length = 255)
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING, PAID
    }
    @PrePersist
    @PreUpdate
    private void calculateTotalAmount() {
        if (this.penalty != null && this.penalty.getAmount() != null) {
            this.totalAmount = this.penalty.getAmount();
        } else {
            this.totalAmount = BigDecimal.ZERO;
        }
    }
}

