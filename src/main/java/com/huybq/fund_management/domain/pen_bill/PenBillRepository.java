package com.huybq.fund_management.domain.pen_bill;

import com.huybq.fund_management.domain.contributions.Contribution;
import com.huybq.fund_management.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Query("SELECT new com.huybq.fund_management.domain.pen_bill.BillStatisticsDTO(" +
            "YEAR( p.dueDate), COALESCE(SUM(p.totalAmount), 0) )" +
            "FROM PenBill p " +
            "WHERE p.paymentStatus = 'PAID' AND YEAR( p.dueDate) = :year " +
            "GROUP BY YEAR( p.dueDate) ")
    BillStatisticsDTO getPenaltyStatisticsByYear(@Param("year") int year);


    // Tổng số tiền phạt đã thanh toán trong một năm
    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) " +
            "FROM PenBill p " +
            "WHERE p.paymentStatus = 'PAID' AND FUNCTION('YEAR', p.dueDate) = :year")
    BigDecimal getTotalPaidPenaltiesByYear(@Param("year") int year);

    @Query("""
                SELECT p.user, SUM(p.totalAmount)
                FROM PenBill p
                WHERE FUNCTION('YEAR', p.dueDate) = :year
                  AND FUNCTION('MONTH', p.dueDate) = :month
                  AND p.paymentStatus = 'UNPAID'
                GROUP BY p.user,p.createdAt
            """)
    List<Object[]> findUserAndTotalUnpaidAmountByMonthAndYear(
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("""
                SELECT p
                FROM PenBill p
                JOIN FETCH p.user
                JOIN FETCH p.penalty
                WHERE DATE(p.createdAt) = :date
                  AND p.paymentStatus = 'UNPAID'
            """)
    List<PenBill> findBillsAndTotalUnpaidAmountInDate(@Param("date") LocalDate date);

}
