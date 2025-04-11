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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderService {


    private final PeriodRepository periodRepository;

    private final ReminderRepository reminderRepository;

    private final UserRepository userRepository;

    private final Notification notification;

    public List<ReminderResponseDTO> getAllReminders() {
//        Set<String> uniqueDescriptions = new HashSet<>();
        return reminderRepository.findAllByOrderByScheduledTimeAsc().stream()
                .map(reminder -> ReminderResponseDTO.builder()
                        .id(reminder.getId())
                        .title(reminder.getTitle())
                        .description(reminder.getDescription())
                        .type(reminder.getReminderType().name())
                        .status(reminder.getStatus().name())
                        .users(reminder.getUsers().stream().toList())
                        .isSendChatGroup(reminder.isSendChatGroup())
                        .scheduledTime(reminder.getScheduledTime())
                        .createdAt(String.valueOf(reminder.getCreatedAt()))
                        .build())
                .toList();
    }

    public Set<User> findUsersByReminderId(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + reminderId));
        return reminder.getUsers();
    }
//    public List<ReminderDTO> getRemindersByUser(Long userId) {
//        return reminderRepository.findByUserId(userId).stream().map(reminder -> ReminderDTO.builder()
//                .id(reminder.getId())
//                .title(reminder.getTitle())
//                .description(reminder.getDescription())
//                .type(reminder.getReminderType().name())
//                .status(reminder.getStatus().name())
//                .build()).toList();
//    }

//    @Scheduled(cron = "0 0 0 7 * ?", zone = "Asia/Ho_Chi_Minh")

    /// /    @Scheduled(cron = "0 35 10 26 * ?", zone = "Asia/Ho_Chi_Minh")
//    public void scheduleMonthlyReminderCreation() {
//        LocalDate now = LocalDate.now();
//        int month = now.getMonthValue();
//        int year = now.getYear();
//        createMonthlyReminders(month, year);
//    }

//    @Transactional
//    public void createMonthlyReminders(int month, int year) {
//
//        List<User> allUsers = userRepository.findAll();
//
//        createReminderPayFund(month, year, allUsers);
//    }

//    @Transactional
//    public void createRemindersForUserNotContributionOrOwed(int month, int year) {
//
//        List<User> users = userRepository.findUsersOwedContributed(month, year);
//
//        createReminderPayFund(month, year, users);
//    }

//    private void createReminderPayFund(int month, int year, List<User> users) {
//        StringBuilder message = new StringBuilder();
//        message.append("@all\n🔔 **Nhắc nhở đóng quỹ tháng ").append(month).append("/").append(year).append("**\n\n");
//        message.append("| STT | TÊN | TIỀN NỢ |\n");
//        message.append("|---|---|---|\n");
//
//        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")); // Format tiền VND
//
//        int index = 1;
//        for (User user : users) {
//            BigDecimal owedAmount = periodRepository.getTotalPeriodAmountByMonthAndYear(month, year);
//
//            if (owedAmount.compareTo(BigDecimal.ZERO) > 0) {
//                String formattedAmount = currencyFormat.format(owedAmount); // Định dạng số tiền
//
//                message.append("| ").append(index++).append(" | ")
//                        .append(user.getFullName()).append(" | ")
//                        .append(formattedAmount).append(" |\n");
//
//                Reminder reminder = new Reminder();
//                reminder.setUser(user);
//                reminder.setTitle("Nhắc nhở đóng quỹ");
//                reminder.setDescription("Bạn đang nợ quỹ tháng " + month + "/" + year + ": " + formattedAmount);
//                reminder.setOwedAmount(owedAmount);
//                reminder.setReminderType(Reminder.ReminderType.CONTRIBUTION);
//                reminder.setStatus(Reminder.Status.SENT);
//                reminderRepository.save(reminder);
//            }
//        }
//
//        // Gửi thông báo tổng hợp nếu có nợ
//        if (index > 1) {
//            notification.sendNotification(message.toString(),"java");
//        }
//    }
    public void createReminder(ReminderDTO reminderDTO) {
        Reminder reminder = new Reminder();
        reminder.setUsers(new HashSet<>(getUserFromDTO(reminderDTO)));
        reminder.setTitle(reminderDTO.title());
        reminder.setDescription(reminderDTO.description());
        reminder.setReminderType(Reminder.ReminderType.valueOf(reminderDTO.type()));
        reminder.setStatus(Reminder.Status.SENT);
        reminder.setScheduledTime(reminderDTO.scheduledTime());
        reminder.setSendChatGroup(reminderDTO.isSendChatGroup());

        reminderRepository.save(reminder);

        if (reminderDTO.isSendChatGroup()&&(reminderDTO.scheduledTime() == null || reminderDTO.scheduledTime().isBefore(LocalDateTime.now()))) {
            sendNotification(reminder);  // Gửi reminder ngay lập tức nếu cần
        }

//        // Nếu cần gửi thông báo vào nhóm chat
//        if (reminderDTO.isSendChatGroup()) {
//           sendNotification(reminder);
//        }
    }

    public void sendNotification(Reminder reminder) {
        Set<User> users = reminder.getUsers();
        String mention;

        boolean isAllUsers = isAllUsersSelected(users); // Bạn cần hàm này kiểm tra xem có phải tất cả user không

        if (isAllUsers) {
            mention = "@all";
        } else {
            mention = users.stream()
                    .map(user -> "@" + user.getEmail().replace("@", "-"))
                    .collect(Collectors.joining(" "));
        }

        String message = mention + "\n🔔 Thông báo mới: " + reminder.getTitle() +
                "\nNội dung: " + reminder.getDescription();

        notification.sendNotification(message, "java");

        reminder.setStatus(Reminder.Status.READ);
        reminderRepository.save(reminder);
    }

    private boolean isAllUsersSelected(Set<User> selectedUsers) {
        long totalUsers = userRepository.findAllByIsDeleteIsFalse().size();
        return selectedUsers.size() == totalUsers;
    }


    @Scheduled(fixedRate = 60000)
    public void processScheduledReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> reminders = reminderRepository.findByScheduledTimeBeforeAndStatus(now, Reminder.Status.UNSENT);
        reminders.forEach(reminder->{
            sendNotification(reminder);
            reminder.setStatus(Reminder.Status.SENT);
            reminderRepository.save(reminder);
        });

    }


//    private void sendReminder(Reminder reminder) {
//        String message = "🔔 " + reminder.getTitle() + "\n" + reminder.getDescription();
//        notification.sendNotification(message, "java");
//        reminder.setStatus(Reminder.Status.READ);
//        reminderRepository.save(reminder);
//    }

    @Transactional
    public void updateReminder(Long id, ReminderDTO dto) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));


        List<User> users = getUserFromDTO(dto);

        reminder.setUsers(new HashSet<>(users));

        reminder.setTitle(dto.title());
        reminder.setDescription(dto.description());
        reminder.setReminderType(Reminder.ReminderType.valueOf(dto.type()));
        reminder.setStatus(Reminder.Status.SENT);  // Giả sử status là PENDING
        reminder.setScheduledTime(dto.scheduledTime());
        reminder.setSendChatGroup(dto.isSendChatGroup());

        reminderRepository.save(reminder);

        if (dto.isSendChatGroup()&&(dto.scheduledTime() == null || dto.scheduledTime().isBefore(LocalDateTime.now()))) {
            sendNotification(reminder);  // Gửi reminder ngay lập tức nếu cần
        }
    }

    private List<User> getUserFromDTO(ReminderDTO dto) {
        if ((dto.userIds() == null || dto.userIds().isEmpty())) {
            return userRepository.findAllByIsDeleteIsFalse();
        } else {
            return userRepository.findAllById(dto.userIds()).stream()
                    .filter(user -> !user.isDelete())
                    .collect(Collectors.toList());
        }
    }

//    @Transactional
//    public void updateAllReminders(ReminderDTO dto) {
//        List<Reminder> reminders = reminderRepository.findAll();
//
//        for (Reminder reminder : reminders) {
//            reminder.setTitle(dto.title());
//            reminder.setDescription(dto.description());
//            reminder.setScheduledTime(dto.scheduledTime());
//            reminder.setSendChatGroup(dto.isSendChatGroup());
//            reminderRepository.save(reminder);
//        }
//
//        if (dto.isSendChatGroup()) {
//            notification.sendNotification("@all\n" + "Cập nhật thông báo: **" + dto.title() + "**\n\n" + dto.description() + "\n\n #reminder", "java");
//        }
//    }


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
