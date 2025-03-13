package com.huybq.fund_management.domain.contributions;

import com.huybq.fund_management.domain.user.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {
    List<Contribution> findAllByPeriodId(Long periodId);

    @Query("SELECT DISTINCT c.user FROM Contribution c WHERE c.period.id = :periodId AND c.totalAmount > 0")
    List<User> findUsersByPeriodId(@Param("periodId") Long periodId);

    List<Contribution> findAllByPeriod_MonthAndPeriod_Year(Integer month, Integer year);

    List<Contribution> findByUserId(Long userId);

    List<Contribution> findByUserIdAndPaymentStatus(Long userId, Contribution.PaymentStatus paymentStatus);

    boolean existsByUserIdAndPeriodId(@NotNull(message = "userId is required") Long userId, @NotNull(message = "periodId is required") Long periodId);

    Optional<Contribution> findByUserIdAndPeriodId(@NotNull(message = "userId is required") Long userId, @NotNull(message = "periodId is required") Long periodId);
}
