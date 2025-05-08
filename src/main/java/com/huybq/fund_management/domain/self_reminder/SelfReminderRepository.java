package com.huybq.fund_management.domain.self_reminder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SelfReminderRepository extends JpaRepository<SelfReminder, Long> {
    List<SelfReminder> findByUserId(Long userId);
    long countByUserId(Long userId);

    List<SelfReminder> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<SelfReminder> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime start);

    List<SelfReminder> findByUserIdAndCreatedAtBefore(Long userId, LocalDateTime end);

}

