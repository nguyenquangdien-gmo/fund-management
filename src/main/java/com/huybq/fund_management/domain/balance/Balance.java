package com.huybq.fund_management.domain.balance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
public class Balance {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(nullable = false,unique = true)
    private String title;
    private BigDecimal totalAmount;
}
