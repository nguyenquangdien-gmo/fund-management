package com.huybq.fund_management.domain.reminder;

import com.huybq.fund_management.domain.contributions.ContributionRepository;
import com.huybq.fund_management.domain.period.Period;
import com.huybq.fund_management.domain.period.PeriodRepository;
import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ContributionRepository contributionRepository;

    private final PeriodRepository periodRepository;

    private final ReminderRepository reminderRepository;

    private final UserRepository userRepository;

    public List<ReminderDTO> getAllUniqueReminders() {
        Set<String> uniqueDescriptions = new HashSet<>();

        return reminderRepository.findAll().stream()
                .filter(reminder -> uniqueDescriptions.add(reminder.getDescription())) // Chỉ thêm nếu chưa tồn tại
                .map(reminder -> ReminderDTO.builder()
                        .id(reminder.getId())
                        .title(reminder.getTitle())
                        .description(reminder.getDescription())
                        .type(reminder.getReminderType().name())
                        .status(reminder.getStatus().name())
                        .createdAt(String.valueOf(reminder.getCreatedAt()))
                        .build())
                .toList();
    }



    public List<ReminderDTO> getRemindersByUser(Long userId) {
        return reminderRepository.findByUserIdAndStatus(userId, Reminder.Status.SENT).stream().map(reminder -> ReminderDTO.builder()
                .id(reminder.getId())
                .title(reminder.getTitle())
                .description(reminder.getDescription())
                .type(reminder.getReminderType().name())
                .status(reminder.getStatus().name())
                .build()).toList();
    }

    @Scheduled(cron = "0 0 0 7 * ?")
    public void scheduleMonthlyReminderCreation() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();
        createMonthlyReminders(month, year);
    }

    @Transactional
    public void createMonthlyReminders(int month, int year) {

        List<User> allUsers = userRepository.findAll();

        createReminder(month, year, allUsers);
    }

    @Transactional
    public void createRemindersForUserNotContributionOrOwed(int month, int year) {

        List<User> users = userRepository.findUsersOwedContributed(month, year);

        createReminder(month, year, users);
    }

    private void createReminder(int month, int year, List<User> users) {
        for (User user : users) {
            Long userId = user.getId();
            BigDecimal owedAmount;

            if (contributionRepository.existsByUserIdAndPeriod_MonthAndPeriod_Year(userId, month, year)) {
                owedAmount = contributionRepository.findOwedAmountByUserAndPeriod(userId, month, year)
                        .orElse(BigDecimal.ZERO);
            } else {
                owedAmount = periodRepository.getTotalPeriodAmountByMonthAndYear(month, year);
            }

            if (owedAmount.compareTo(BigDecimal.ZERO) > 0) {
                Reminder reminder = new Reminder();
                reminder.setUser(user);
                reminder.setTitle("Reminder for Contribution");
                reminder.setDescription("You have an outstanding balance of " + owedAmount + " to contribute in " + month + "/" + year);
                ;
                reminder.setOwedAmount(owedAmount);
                reminder.setReminderType(Reminder.ReminderType.CONTRIBUTION);
                reminder.setStatus(Reminder.Status.SENT);
                reminderRepository.save(reminder);
            }
        }
    }

    public void createOtherReminder(ReminderDTO dto) {
        List<User> allUsers = userRepository.findAll();
        allUsers.forEach(user -> {
            Reminder reminder = new Reminder();
            reminder.setUser(user);
            reminder.setTitle(dto.title());
            reminder.setDescription(dto.description());
            reminder.setReminderType(Reminder.ReminderType.OTHER);
            reminder.setStatus(Reminder.Status.SENT);
            reminderRepository.save(reminder);
        });

    }

    public void markAsRead(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));

        if (reminder.getStatus() != Reminder.Status.READ) {
            reminder.setStatus(Reminder.Status.READ);
            reminderRepository.save(reminder);
        }
    }

    @Transactional
    public void markAsReadMulti(List<Long> reminderIds) {
        List<Reminder> reminders = reminderRepository.findAllById(reminderIds);

        if (reminders.isEmpty()) {
            throw new ResourceNotFoundException("No reminders found with the given IDs.");
        }

        for (Reminder reminder : reminders) {
            if (reminder.getStatus() != Reminder.Status.READ) {
                reminder.setStatus(Reminder.Status.READ);
            }
        }

        reminderRepository.saveAll(reminders);
    }

    @Transactional
    public void deleteReminder(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));
        reminderRepository.delete(reminder);
    }
}
