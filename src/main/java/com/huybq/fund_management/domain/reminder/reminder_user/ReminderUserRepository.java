package com.huybq.fund_management.domain.reminder.reminder_user;

import com.huybq.fund_management.domain.reminder.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderUserRepository extends JpaRepository<ReminderUser, ReminderUserId> {
    boolean existsByReminderIdAndUserIdAndCompletedTrue(Long reminderId, Long userId);

    List<ReminderUser> findByUserIdOrderByReminderCreatedAtDesc(Long userId);

    Optional<ReminderUser> findReminderUserByReminder_IdAndUser_Id(Long reminderId, Long userId);
}
