package com.huybq.fund_management.domain.period;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Period {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(name = "standard_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal standardAmount;

    @Column(name = "common_fund", nullable = false, precision = 10, scale = 2)
    private BigDecimal commonFund;

    @Column(name = "snack_fund", nullable = false, precision = 10, scale = 2)
    private BigDecimal snackFund;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
