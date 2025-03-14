package com.huybq.fund_management.domain.period;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface PeriodRepository extends JpaRepository<Period, Long> {
    Optional<Period> findByMonthAndYear(int month, int year);
    @Query("SELECT SUM(f.totalAmount) FROM Period f WHERE f.month = :month AND f.year = :year")
    BigDecimal getTotalPeriodAmountByMonthAndYear(@Param("month") int month, @Param("year") int year);

}
