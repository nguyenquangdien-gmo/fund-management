package com.huybq.fund_management.domain.contributions;

import com.huybq.fund_management.domain.period.Period;
import com.huybq.fund_management.domain.user.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {
    List<Contribution> findAllByPeriodId(Long periodId);

    @Query("SELECT DISTINCT c.user FROM Contribution c WHERE c.period.id = :periodId AND c.totalAmount > 0")
    List<User> findUsersByPeriodId(@Param("periodId") Long periodId);

    List<Contribution> findAllByPeriod_MonthAndPeriod_Year(Integer month, Integer year);

    List<Contribution> findByUserId(Long userId);

    List<Contribution> findByUserIdAndPaymentStatusOrderByCreatedAtDesc(Long userId, Contribution.PaymentStatus paymentStatus);


    @Query("SELECT c.period.month, COALESCE(SUM(c.totalAmount), 0) " +
            "FROM Contribution c " +
            "WHERE c.period.year = :year " +
            "GROUP BY c.period.month " +
            "ORDER BY c.period.month ASC")
    List<Object[]> getMonthlyContributionStatistics(@Param("year") int year);

    @Query("SELECT FUNCTION('YEAR', c.updatedAt), COALESCE(SUM(c.totalAmount), 0) " +
            "FROM Contribution c " +
            "WHERE c.paymentStatus = 'PAID' " +
            "GROUP BY FUNCTION('YEAR', c.updatedAt) " +
            "ORDER BY FUNCTION('YEAR', c.updatedAt) DESC")
    List<Object[]> getYearlyContributionStatistics();

    @Query("SELECT c.user, c.totalAmount,c.createdAt " +
            "FROM Contribution c WHERE c.isLate = true")
    List<Object[]> getLateContributors();

    List<Contribution> findByUserIdAndPeriodId(@NotNull(message = "userId is required") Long userId, @NotNull(message = "periodId is required") Long periodId);

    @Query("SELECT COALESCE(SUM(c.totalAmount), 0) " +
            "FROM Contribution c " +
            "WHERE c.paymentStatus = 'PAID' AND c.period.year = :year")
    BigDecimal getTotalPaidContributionsByYear(@Param("year") int year);

    List<Contribution> findAllByOrderByCreatedAtDesc();



}
