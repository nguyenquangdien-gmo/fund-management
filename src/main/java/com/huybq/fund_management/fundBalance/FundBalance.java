package com.huybq.fund_management.fundBalance;

import com.huybq.fund_management.fund.Fund;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FundBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
    @ManyToOne
    @JoinColumn(name = "fund_id",nullable = false)
    private Fund fund;
    private int month;
    private int year;
}
