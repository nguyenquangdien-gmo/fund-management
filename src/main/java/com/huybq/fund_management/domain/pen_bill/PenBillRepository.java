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
import java.util.Optional;

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
            "ELSE 4 END, p.createdAt ASC")
    List<PenBill> findAllOrderByStatusPriority();

    // Thống kê tổng tiền phạt theo từng tháng trong một năm
    @Query("SELECT FUNCTION('MONTH', p.createdAt), COALESCE(SUM(p.totalAmount), 0) " +
            "FROM PenBill p " +
            "WHERE FUNCTION('YEAR', p.createdAt) = :year AND p.paymentStatus = 'PAID' " +
            "GROUP BY FUNCTION('MONTH', p.createdAt) " +
            "ORDER BY FUNCTION('MONTH', p.createdAt) ASC")
    List<Object[]> getMonthlyPenaltyStatistics(@Param("year") int year);

    // Thống kê tổng tiền phạt theo từng năm
    @Query("SELECT new com.huybq.fund_management.domain.pen_bill.BillStatisticsDTO(" +
            "YEAR( p.createdAt), COALESCE(SUM(p.totalAmount), 0) )" +
            "FROM PenBill p " +
            "WHERE p.paymentStatus = 'PAID' AND YEAR( p.createdAt) = :year " +
            "GROUP BY YEAR( p.createdAt) ")
    BillStatisticsDTO getPenaltyStatisticsByYear(@Param("year") int year);


    // Tổng số tiền phạt đã thanh toán trong một năm
    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) " +
            "FROM PenBill p " +
            "WHERE p.paymentStatus = 'PAID' AND FUNCTION('YEAR', p.createdAt) = :year")
    BigDecimal getTotalPaidPenaltiesByYear(@Param("year") int year);

    @Query("""
                SELECT p.user, SUM(p.totalAmount)
                FROM PenBill p
                WHERE FUNCTION('YEAR', p.createdAt) = :year
                  AND FUNCTION('MONTH', p.createdAt) = :month
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


    @Query("""
        SELECT p FROM PenBill p 
        WHERE p.user.id = :userId 
          AND p.penalty.id = :penaltyId 
          AND DATE(p.createdAt) = :createdDate
    """)
    Optional<PenBill> findByUserAndPenaltyAndCreatedDate(
            @Param("userId") Long userId,
            @Param("penaltyId") Long penaltyId,
            @Param("createdDate") LocalDate createdDate
    );

}
