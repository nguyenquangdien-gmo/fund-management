package com.huybq.fund_management.domain.reminder.reminder_user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReminderUserRepository extends JpaRepository<ReminderUser, ReminderUserId> {
    boolean existsByReminderIdAndUserIdAndCompletedTrue(Long reminderId, Long userId);
}
