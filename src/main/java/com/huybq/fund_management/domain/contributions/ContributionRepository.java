package com.huybq.fund_management.domain.contributions;

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

    //lấy ra tiền nợ của user trong 1 tháng
    @Query("SELECT c.owedAmount FROM Contribution c WHERE c.user.id = :userId AND c.period.month = :month AND c.period.year = :year")
    Optional<BigDecimal> findOwedAmountByUserAndPeriod(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);

    @Query("SELECT DISTINCT c.user FROM Contribution c WHERE c.period.id = :periodId AND c.totalAmount > 0")
    List<User> findUsersByPeriodId(@Param("periodId") Long periodId);

    List<Contribution> findAllByPeriod_MonthAndPeriod_Year(Integer month, Integer year);

    List<Contribution> findByUserId(Long userId);

    List<Contribution> findByUserIdAndPaymentStatus(Long userId, Contribution.PaymentStatus paymentStatus);
    // kiểm tra xem user đã đóng tiền trong tháng này chưa
    boolean existsByUserIdAndPeriod_MonthAndPeriod_Year(Long userId, int month, int year);

    Optional<Contribution> findByUserIdAndPeriodId(@NotNull(message = "userId is required") Long userId, @NotNull(message = "periodId is required") Long periodId);
}
