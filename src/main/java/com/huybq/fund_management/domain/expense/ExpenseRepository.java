package com.huybq.fund_management.domain.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserId(Long userId);
    @Query("SELECT e FROM Expense e WHERE YEAR(e.createdAt) = :year AND MONTH(e.createdAt) = :month")
    List<Expense> findByMonthAndYear(@Param("month") int month, @Param("year") int year);

    // tong số tiền đã chi theo tháng/năm
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE YEAR(e.createdAt) = :year AND MONTH(e.createdAt) = :month")
    BigDecimal getTotalExpenseByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE YEAR(e.createdAt) = :year")
    BigDecimal getTotalExpenseByYear( @Param("year") int year);

}
