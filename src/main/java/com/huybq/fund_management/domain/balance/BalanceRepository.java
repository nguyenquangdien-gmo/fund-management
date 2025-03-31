package com.huybq.fund_management.domain.balance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Integer> {
    Optional<Balance> findBalanceByTitle(String title);

    Collection<Object> findByTitle(String title);
}
