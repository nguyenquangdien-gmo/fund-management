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
    List<Work> findByUserId(Long userId);

    @Query("SELECT w FROM Work w WHERE w.user.id = :userId AND " +
            "((MONTH(w.fromDate) = :month AND YEAR(w.fromDate) = :year) OR " +
            "(MONTH(w.endDate) = :month AND YEAR(w.endDate) = :year))")
    List<Work> findByUserIdAndMonthAndYear(Long userId, int month, int year);

    @Query("SELECT w FROM Work w WHERE :date BETWEEN w.fromDate AND w.endDate")
    List<Work> findByDate(LocalDate date);

    @Query("SELECT w FROM Work w WHERE w.user.id = :userId AND w.type = :type AND " +
            "((MONTH(w.fromDate) = :month AND YEAR(w.fromDate) = :year) OR " +
            "(MONTH(w.endDate) = :month AND YEAR(w.endDate) = :year))")
    List<Work> findWorksByUserAndMonthWithType(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month,@Param("type") StatusType type);

    @Query("SELECT w FROM Work w WHERE w.user.id = :userId AND " +
            "((MONTH(w.fromDate) = :month AND YEAR(w.fromDate) = :year) OR " +
            "(MONTH(w.endDate) = :month AND YEAR(w.endDate) = :year))")
    List<Work> findWorksByUserAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);


    @Query("SELECT w FROM Work w WHERE w.user.id = :userId AND :date BETWEEN w.fromDate AND w.endDate")
    Optional<Work> findWorkByUserAndDate(Long userId, LocalDate date);

    @Query("SELECT COUNT(w) > 0 FROM Work w " +
            "WHERE w.user.id = :userId " +
            "AND (w.fromDate <= :endDate AND w.endDate >= :fromDate)")
    boolean existsByUserIdAndDateRangeOverlap(@Param("userId") Long userId,
                                              @Param("fromDate") LocalDate fromDate,
                                              @Param("endDate") LocalDate endDate);


}
