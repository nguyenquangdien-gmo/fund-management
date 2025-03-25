package com.huybq.fund_management.domain.late;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LateRepository extends JpaRepository<Late, Long> {
    void deleteByDate(LocalDate date);
    @Query("SELECT l FROM Late l WHERE l.date BETWEEN :fromDate AND :toDate")
    List<Late> findByDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
    @Query("SELECT l.user, COUNT(l) FROM Late l " +
            "WHERE MONTH(l.date) = :month AND YEAR(l.date) = :year " +
            "GROUP BY l.user " +
            "HAVING COUNT(l) > :minLateCount")
    List<Object[]> findUsersWithLateCountInMonth(@Param("month") int month,
                                                 @Param("year") int year,
                                                 @Param("minLateCount") int minLateCount);
}

