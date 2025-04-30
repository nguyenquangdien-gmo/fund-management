package com.huybq.fund_management.domain.reminder;

import com.huybq.fund_management.domain.reminder.reminder_user.ReminderUser;
import com.huybq.fund_management.domain.reminder.reminder_user.ReminderUserId;
import com.huybq.fund_management.domain.reminder.reminder_user.ReminderUserRepository;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserMapper;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.domain.user.UserResponseDTO;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import com.huybq.fund_management.utils.chatops.Notification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderService {
    private final ReminderRepository reminderRepository;

    private final UserRepository userRepository;

    private final ReminderUserRepository reminderUserRepository;

    private final UserMapper userMapper;

    private final Notification notification;

    public List<ReminderResponseDTO> getAllReminders() {
//        Set<String> uniqueDescriptions = new HashSet<>();
        return reminderRepository.findAllByOrderByScheduledTimeDesc().stream()
                .map(reminder -> ReminderResponseDTO.builder()
                        .id(reminder.getId())
                        .title(reminder.getTitle())
                        .description(reminder.getDescription())
                        .type(reminder.getReminderType().name())
                        .status(reminder.getStatus().name())
                        .users(reminder.getReminderUsers().stream()
                                .map(userReminder -> userMapper.toResponseDTO(userReminder.getUser()))
                                .toList())
                        .isSendChatGroup(reminder.isSendChatGroup())
                        .scheduledTime(reminder.getScheduledTime())
                        .createdAt(String.valueOf(reminder.getCreatedAt()))
                        .build())
                .toList();
    }

    public ReminderResponseDTO getReminderById(Long id) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));
        return ReminderResponseDTO.builder()
                .id(reminder.getId())
                .title(reminder.getTitle())
                .description(reminder.getDescription())
                .type(reminder.getReminderType().name())
                .status(reminder.getStatus().name())
                .users(null)
                .isSendChatGroup(reminder.isSendChatGroup())
                .scheduledTime(reminder.getScheduledTime())
                .createdAt(String.valueOf(reminder.getCreatedAt()))
                .build();
    }


    public Set<UserResponseDTO> findUsersByReminderId(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + reminderId));
        return reminder.getReminderUsers().stream()
                .map(userReminder -> userMapper.toResponseDTO(userReminder.getUser()))
                .collect(Collectors.toSet());
    }

    public void createReminder(ReminderDTO reminderDTO, String createdEmail) {

        Reminder reminder = new Reminder();

        List<User> users = getUserFromDTO(reminderDTO);
        List<ReminderUser> reminderUsers = users.stream()
                .map(user -> new ReminderUser(reminder, user))
                .toList();

        reminder.setReminderUsers(reminderUsers);
        reminder.setTitle(reminderDTO.title());
        reminder.setDescription(reminderDTO.description());
        reminder.setReminderType(Reminder.ReminderType.valueOf(reminderDTO.type()));
        reminder.setStatus(Reminder.Status.SENT);
        reminder.setScheduledTime(reminderDTO.scheduledTime());
        reminder.setSendChatGroup(reminderDTO.isSendChatGroup());
        reminder.setCreatedEmail(createdEmail);

        reminderRepository.save(reminder);

        if (Optional.ofNullable(reminderDTO.scheduledTime())
                .map(t -> t.isBefore(LocalDateTime.now()))
                .orElse(true)) {
            if (reminderDTO.isSendChatGroup()) {
                sendNotification(reminder);
            } else {
                sendForMember(reminder);
            }
        }
    }

    private void sendForMember(Reminder reminder) {
        for (User user : reminder.getReminderUsers().stream().map(ReminderUser::getUser).collect(Collectors.toSet())) {
            StringBuilder message = new StringBuilder("@" + user.getEmail().replace("@", "-") + "\n🔔 Thông báo mới: " + reminder.getTitle())
                    .append("\nNội dung: " + reminder.getDescription());
            notification.sendNotificationForMember(message.toString(), reminder.getCreatedEmail(), user.getEmail());
        }
    }


    public void sendNotification(Reminder reminder) {
        Set<User> users = reminder.getReminderUsers().stream()
                .map(ReminderUser::getUser).collect(Collectors.toSet());
        String mention;

        boolean isAllUsers = isAllUsersSelected(users);

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
    }

    private boolean isAllUsersSelected(Set<User> selectedUsers) {
        long totalUsers = userRepository.findAllByIsDeleteIsFalse().size();
        return selectedUsers.size() == totalUsers;
    }


    @Scheduled(fixedRate = 60000)
    public void processScheduledReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMinute = now.withSecond(0).withNano(0);
        LocalDateTime endOfMinute = now.withSecond(59).withNano(999_999_999);


        List<Reminder> reminders = reminderRepository
                .findByScheduledTimeBetweenAndStatus(startOfMinute, endOfMinute, Reminder.Status.SENT);

        reminders.forEach(reminder -> {
            if (reminder.getLastSentDate() == null || !reminder.getLastSentDate().toLocalDate().isEqual(now.toLocalDate())) {
                if (reminder.isSendChatGroup()) {
                    sendNotification(reminder);
                } else {
                    sendForMember(reminder);
                }
                reminder.setLastSentDate(now);
                reminderRepository.save(reminder);
            }
        });
    }

    @Transactional
    public void completeSurvey(Long reminderId, Long userId) {
        ReminderUserId id = new ReminderUserId(reminderId, userId);

        ReminderUser reminderUser = reminderUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found for this user."));

        Reminder reminder = reminderUser.getReminder();
        reminderRepository.save(reminder);
        reminderUser.setCompleted(true);
        reminderUser.setStatus(Reminder.Status.READ);
        reminderUser.setFinishedAt(LocalDateTime.now());

        reminderUserRepository.save(reminderUser);
    }

    public List<SurveyStatusDTO> getSurveyResults(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + reminderId));

        return reminder.getReminderUsers().stream()
                .map(ru ->
                        new SurveyStatusDTO(ru.getUser().getId(), ru.getUser().getFullName(), ru.isCompleted(), ru.getFinishedAt())
                ).collect(Collectors.toList());
    }

//    private void sendReminder(Reminder reminder) {
//        String message = "🔔 " + reminder.getTitle() + "\n" + reminder.getDescription();
//        notification.sendNotification(message, "java");
//        reminder.setStatus(Reminder.Status.READ);
//        reminderRepository.save(reminder);
//    }

    public boolean hasUserCompletedSurvey(Long reminderId, Long userId) {
        return reminderUserRepository.existsByReminderIdAndUserIdAndCompletedTrue(reminderId, userId);
    }

    @Transactional
    public void updateReminder(Long id, ReminderDTO dto) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));

        List<User> users = getUserFromDTO(dto);

        reminder.getReminderUsers().clear();

        List<ReminderUser> newReminderUsers = users.stream()
                .map(user -> new ReminderUser(reminder, user))
                .toList();

        reminder.setReminderUsers(newReminderUsers);

        reminder.setTitle(dto.title());
        reminder.setDescription(dto.description());
        reminder.setReminderType(Reminder.ReminderType.valueOf(dto.type()));
        reminder.setStatus(Reminder.Status.SENT);
        reminder.setScheduledTime(dto.scheduledTime());
        reminder.setSendChatGroup(dto.isSendChatGroup());

        reminderRepository.save(reminder);

        if (dto.isSendChatGroup() && (dto.scheduledTime() == null || dto.scheduledTime().isBefore(LocalDateTime.now()))) {
            sendNotification(reminder);
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
