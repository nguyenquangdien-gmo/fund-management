package com.huybq.fund_management.domain.trans;

import com.huybq.fund_management.domain.period.Period;
import com.huybq.fund_management.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trans {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(length = 255)
    private String description;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    @ManyToOne
    @JoinColumn(name = "period_id")
    private Period period;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum TransactionType {
        INCOME_FUND,INCOME_PENALTY, EXPENSE
    }
}
