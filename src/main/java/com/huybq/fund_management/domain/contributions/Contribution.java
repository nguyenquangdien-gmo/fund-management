package com.huybq.fund_management.domain.contributions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huybq.fund_management.domain.period.Period;
import com.huybq.fund_management.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "period_id", nullable = false)
    private Period period;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(length = 255)
    private String note;

    private BigDecimal owedAmount= BigDecimal.valueOf(0);
    private BigDecimal overpaidAmount= BigDecimal.valueOf(0);
    private Boolean isLate;

    @JsonIgnore
    @Column(name = "previous_total_amount")
    private BigDecimal previousTotalAmount;

    @JsonIgnore
    @Column(name = "previous_owed_amount")
    private BigDecimal previousOwedAmount;

    @JsonIgnore
    @Column(name = "previous_overpaid_amount")
    private BigDecimal previousOverpaidAmount;

    @JsonIgnore
    @Column(name = "previous_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus previousStatus;



    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        PENDING,UPDATE ,PAID, LATE, PARTIAL, CANCELED
    }
}