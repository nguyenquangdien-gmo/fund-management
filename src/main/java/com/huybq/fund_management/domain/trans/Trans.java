package com.huybq.fund_management.domain.trans;

import com.huybq.fund_management.domain.contributions.Contribution;
import com.huybq.fund_management.domain.fund.Fund;
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
public class Trans {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(length = 255)
    private String description;

    @ManyToOne
    @JoinColumn(name = "contribution_id")
    private Contribution contribution;

    @ManyToOne
    @JoinColumn(name = "fine_id")
    private Penalty penalty;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum TransactionType {
        INCOME, EXPENSE
    }
}
