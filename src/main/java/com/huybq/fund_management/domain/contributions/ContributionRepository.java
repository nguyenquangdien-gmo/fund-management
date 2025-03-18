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

    @Query("SELECT c.owedAmount FROM Contribution c WHERE c.user.id = :userId AND c.period.month = :month AND c.period.year = :year")
    Optional<BigDecimal> findOwedAmountByUserAndPeriod(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);

    @Query("SELECT DISTINCT c.user FROM Contribution c WHERE c.period.id = :periodId AND c.totalAmount > 0")
    List<User> findUsersByPeriodId(@Param("periodId") Long periodId);

    List<Contribution> findAllByPeriod_MonthAndPeriod_Year(Integer month, Integer year);

    List<Contribution> findByUserId(Long userId);

    List<Contribution> findByUserIdAndPaymentStatus(Long userId, Contribution.PaymentStatus paymentStatus);

    boolean existsByUserIdAndPeriod_MonthAndPeriod_Year(Long userId, int month, int year);

    @Query("SELECT c.period.month, COALESCE(SUM(c.totalAmount), 0) " +
            "FROM Contribution c " +
            "WHERE c.period.year = :year " +
            "GROUP BY c.period.month " +
            "ORDER BY c.period.month ASC")
    List<Object[]> getMonthlyContributionStatistics(@Param("year") int year);

    @Query("SELECT c.period.year, COALESCE(SUM(c.totalAmount), 0) " +
            "FROM Contribution c GROUP BY c.period.year " +
            "ORDER BY c.period.year DESC")
    List<Object[]> getYearlyContributionStatistics();

    @Query("SELECT c.user, c.totalAmount,c.createdAt " +
            "FROM Contribution c WHERE c.isLate = true")
    List<Object[]> getLateContributors();

    Optional<Contribution> findByUserIdAndPeriodId(@NotNull(message = "userId is required") Long userId, @NotNull(message = "periodId is required") Long periodId);

    @Query("SELECT c FROM Contribution c WHERE c.user.id = :userId AND c.owedAmount > 0")
    List<Contribution> findOwedContributionsByUserId(@Param("userId") Long userId);

}
