package com.huybq.fund_management.domain.reminder;

import com.huybq.fund_management.domain.contributions.ContributionRepository;
import com.huybq.fund_management.domain.period.Period;
import com.huybq.fund_management.domain.period.PeriodRepository;
import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import com.huybq.fund_management.utils.chatops.Notification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ContributionRepository contributionRepository;

    private final PeriodRepository periodRepository;

    private final ReminderRepository reminderRepository;

    private final UserRepository userRepository;

    private final Notification notification;

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

    @Scheduled(cron = "0 0 0 7 * ?", zone = "Asia/Ho_Chi_Minh")
//    @Scheduled(cron = "0 35 10 26 * ?", zone = "Asia/Ho_Chi_Minh")
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
        StringBuilder message = new StringBuilder();
        message.append("@all\n🔔 **Nhắc nhở đóng quỹ tháng ").append(month).append("/").append(year).append("**\n\n");
        message.append("| STT | TÊN | TIỀN NỢ |\n");
        message.append("|---|---|---|\n");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")); // Format tiền VND

        int index = 1;
        for (User user : users) {
            BigDecimal owedAmount = periodRepository.getTotalPeriodAmountByMonthAndYear(month, year);

            if (owedAmount.compareTo(BigDecimal.ZERO) > 0) {
                String formattedAmount = currencyFormat.format(owedAmount); // Định dạng số tiền

                message.append("| ").append(index++).append(" | ")
                        .append(user.getFullName()).append(" | ")
                        .append(formattedAmount).append(" |\n");

                Reminder reminder = new Reminder();
                reminder.setUser(user);
                reminder.setTitle("Nhắc nhở đóng quỹ");
                reminder.setDescription("Bạn đang nợ quỹ tháng " + month + "/" + year + ": " + formattedAmount);
                reminder.setOwedAmount(owedAmount);
                reminder.setReminderType(Reminder.ReminderType.CONTRIBUTION);
                reminder.setStatus(Reminder.Status.SENT);
                reminderRepository.save(reminder);
            }
        }

        // Gửi thông báo tổng hợp nếu có nợ
        if (index > 1) {
            notification.sendNotification(message.toString());
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
        notification.sendNotification("@all\n" + "Chào mọi người, có thông báo mới: **" + dto.title() + "**\n\n" + dto.description() + "\n\n #reminder");

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
