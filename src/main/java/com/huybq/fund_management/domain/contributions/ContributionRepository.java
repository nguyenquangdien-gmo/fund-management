package com.huybq.fund_management.domain.contributions;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {
    List<Contribution> findAllByPeriodId(Long periodId);

    List<Contribution> findByUserId(Long userId);

    List<Contribution> findByUserIdAndPaymentStatus(Long userId, Contribution.PaymentStatus paymentStatus);

    boolean existsByUserIdAndPeriodId(@NotNull(message = "userId is required") Long userId, @NotNull(message = "periodId is required") Long periodId);

    Optional<Contribution> findByUserIdAndPeriodId(@NotNull(message = "userId is required") Long userId, @NotNull(message = "periodId is required") Long periodId);
}
