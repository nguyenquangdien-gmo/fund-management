package com.huybq.fund_management.domain.work;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface WorkRepository extends JpaRepository<Work, Long> {
    List<Work> findByUserId(Long userId);

    @Query("SELECT ws FROM Work ws WHERE ws.user.id = :userId AND YEAR(ws.date) = :year AND MONTH(ws.date) = :month")
    List<Work> findByUserIdAndMonth(Long userId, int year, int month);

    List<Work> findByDate(LocalDate date);

    Optional<Work> findByUserIdAndDateAndTimePeriod(Long userId, LocalDate date, TimePeriod timePeriod);

    @Query("SELECT COUNT(ws) FROM Work ws WHERE ws.user.id = :userId AND ws.type = :type AND YEAR(ws.date) = :year AND MONTH(ws.date) = :month")
    Long countByUserIdAndTypeAndMonth(Long userId, StatusType type, int year, int month);
}
