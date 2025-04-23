package com.huybq.fund_management.domain.work;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkRepository extends JpaRepository<Work, Long> {
    @Query("""
    SELECT w FROM Work w
    WHERE w.user.id = :userId
    AND w.timePeriod = :timePeriod
    AND (w.fromDate <= :toDate AND w.toDate >= :fromDate)
""")
    List<Work> findOverlappingWorks(@Param("userId") Long userId,
                                    @Param("fromDate") LocalDate fromDate,
                                    @Param("toDate") LocalDate toDate,
                                    @Param("timePeriod") TimePeriod timePeriod);

    @Query("""
    SELECT w FROM Work w
    WHERE w.user.id = :userId
    AND (w.fromDate <= :end AND w.toDate >= :start)
""")
    List<Work> findByUserIdAndDateRange(@Param("userId") Long userId,
                                        @Param("start") LocalDate start,
                                        @Param("end") LocalDate end);

    @Query("""
    SELECT w FROM Work w
    WHERE :date BETWEEN w.fromDate AND w.toDate
""")
    List<Work> findByDateRange(@Param("date") LocalDate date);

    List<Work> findByUserId(Long userId);

    List<Work> findByUserIdAndType(Long userId, StatusType type);

    @Query("SELECT w FROM Work w WHERE w.user.id = :userId AND MONTH(w.fromDate) = :month AND YEAR(w.fromDate) = :year")
    List<Work> findByUserAndMonthAndYear(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);

    @Query("SELECT w FROM Work w WHERE :date BETWEEN w.fromDate AND w.toDate")
    List<Work> findByDateInRange(@Param("date") LocalDate date);

    List<Work> findByStatus(Work.Status status);
}
