package com.huybq.fund_management.domain.reminder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByUserIdAndStatus(Long userId, Reminder.Status status);

    List<Reminder> findSentRemindersByUserId(Long userId);
    @Query("""
                SELECT r
                FROM Reminder r
                WHERE r.user.id = :userId AND r.user.isDelete =false
            """)
    List<Reminder> findByUserId(Long userId);

    List<Reminder> findByScheduledTimeBeforeAndStatus(LocalDateTime now, Reminder.Status status);
}
