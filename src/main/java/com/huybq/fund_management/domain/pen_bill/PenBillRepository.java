package com.huybq.fund_management.domain.pen_bill;

import com.huybq.fund_management.domain.contributions.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Month;
import java.util.Collection;
import java.util.List;

@Service
public interface PenBillRepository extends JpaRepository<PenBill, Long> {
    List<PenBill> findByUserId(Long userId);
    List<PenBill> findByUserIdAndPaymentStatus(Long userId, PenBill.Status status);
    List<PenBill> findByPaymentStatusInOrderByCreatedAtDesc(List<PenBill.Status> statuses);
    List<PenBill> findAllByOrderByCreatedAtDesc();

    @Query("SELECT p FROM PenBill p ORDER BY " +
            "CASE p.paymentStatus " +
            "WHEN 'PENDING' THEN 0 " +
            "WHEN 'UNPAID' THEN 1 " +
            "WHEN 'PAID' THEN 2 " +
            "WHEN 'CANCELED' THEN 3 " +
            "ELSE 4 END, p.dueDate ASC")
    List<PenBill> findAllOrderByStatusPriority();

    // Thống kê tổng tiền phạt theo từng tháng trong một năm
    @Query("SELECT FUNCTION('MONTH', p.dueDate), COALESCE(SUM(p.totalAmount), 0) " +
            "FROM PenBill p " +
            "WHERE FUNCTION('YEAR', p.dueDate) = :year AND p.paymentStatus = 'PAID' " +
            "GROUP BY FUNCTION('MONTH', p.dueDate) " +
            "ORDER BY FUNCTION('MONTH', p.dueDate) ASC")
    List<Object[]> getMonthlyPenaltyStatistics(@Param("year") int year);

    // Thống kê tổng tiền phạt theo từng năm
    @Query("SELECT FUNCTION('YEAR', p.dueDate), COALESCE(SUM(p.totalAmount), 0) " +
            "FROM PenBill p " +
            "WHERE p.paymentStatus = 'PAID' " +
            "GROUP BY FUNCTION('YEAR', p.dueDate) " +
            "ORDER BY FUNCTION('YEAR', p.dueDate) DESC")
    List<Object[]> getYearlyPenaltyStatistics();

    // Tổng số tiền phạt đã thanh toán trong một năm
    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) " +
            "FROM PenBill p " +
            "WHERE p.paymentStatus = 'PAID' AND FUNCTION('YEAR', p.dueDate) = :year")
    BigDecimal getTotalPaidPenaltiesByYear(@Param("year") int year);

}
