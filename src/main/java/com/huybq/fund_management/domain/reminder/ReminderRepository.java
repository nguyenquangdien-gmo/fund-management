package com.huybq.fund_management.domain.reminder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
//    List<Reminder> findByUserIdAndStatus(Long userId, Reminder.Status status);
//
//    List<Reminder> findSentRemindersByUserId(Long userId);
//    List<Reminder> findByUserId(@Param("userId") Long userId);
//    // Lấy tất cả reminder của một user
//    @Query("SELECT r FROM Reminder r JOIN r.users u WHERE u.id = :userId")
//    List<Reminder> findRemindersByUserId(@Param("userId") Long userId);
//
//    // Lấy tất cả reminder active (không bị xóa) của một user
//    @Query("SELECT r FROM Reminder r JOIN r.users u WHERE u.id = :userId AND u.isDelete = false")
//    List<Reminder> findActiveRemindersByUserId(@Param("userId") Long userId);
    List<Reminder> findByScheduledTimeBeforeAndStatus(LocalDateTime now, Reminder.Status status);
    List<Reminder> findAllByStatus(Reminder.Status status);
    List<Reminder> findAllByOrderByScheduledTimeAsc();
}
