package com.huybq.fund_management.domain.reminder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByScheduledTimeBeforeAndStatus(LocalDateTime now, Reminder.Status status);
    List<Reminder> findByScheduledTimeBetweenAndStatus(
            LocalDateTime start,
            LocalDateTime end,
            Reminder.Status status
    );

    List<Reminder> findAllByOrderByScheduledTimeDesc();

}
