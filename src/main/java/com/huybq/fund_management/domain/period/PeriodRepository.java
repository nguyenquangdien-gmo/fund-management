package com.huybq.fund_management.domain.period;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PeriodRepository extends JpaRepository<Period, Long> {
    Optional<Period> findByMonthAndYear(int month, int year);

    @Query("SELECT SUM(f.totalAmount) FROM Period f WHERE f.month = :month AND f.year = :year")
    BigDecimal getTotalPeriodAmountByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("""
                SELECT p FROM Period p 
                WHERE NOT EXISTS (
                    SELECT c FROM Contribution c 
                    WHERE c.user.id = :userId 
                    AND c.period.id = p.id 
                    AND c.paymentStatus != 'CANCELED'
                )
            """)
    List<Period> findUnpaidOrCanceledPeriodsByUser(@Param("userId") Long userId);


    @Query("""
                SELECT p FROM Period p 
                WHERE EXISTS (
                    SELECT c FROM Contribution c 
                    WHERE c.user.id = :userId 
                    AND c.period.id = p.id 
                    AND c.paymentStatus != 'PAID'
                )
            """)
    List<Period> findOwedPeriodsByUser(@Param("userId") Long userId);

    @Query("""
                SELECT p FROM Period p 
                WHERE p.year = :year 
                AND NOT EXISTS (
                    SELECT c FROM Contribution c 
                    WHERE c.user.id = :userId 
                    AND c.period.id = p.id 
                    AND c.paymentStatus = 'PAID'
                )
            """)
    List<Period> findUnpaidPeriodsByUserAndYear(@Param("userId") Long userId, @Param("year") int year);

    boolean existsByMonthAndYear(int month, int year);
}
