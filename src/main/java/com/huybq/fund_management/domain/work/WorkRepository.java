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

    @Query("SELECT w FROM Work w WHERE w.user.id = :userId AND MONTH(w.date) = :month AND YEAR(w.date) = :year")
    List<Work> findByUserIdAndMonthAndYear(Long userId, int month, int year);

    @Query("SELECT w FROM Work w WHERE w.date = :date")
    List<Work> findByDate(LocalDate date);

    @Query("SELECT w FROM Work w WHERE w.user.id = :userId AND w.type = :type AND MONTH(w.date) = :month AND YEAR(w.date) = :year")
    List<Work> findWFHByUserAndMonth(Long userId, int month, int year, StatusType type);

    @Query("SELECT w FROM Work w WHERE w.user.id = :userId AND MONTH(w.date) = :month AND YEAR(w.date) = :year")
    List<Work> findWorksByUserAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT w FROM Work w WHERE w.user.id = :userId AND w.date = :date")
    Optional<Work> findWorkByUserAndDate(Long userId, LocalDate date);

}
