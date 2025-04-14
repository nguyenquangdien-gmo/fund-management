package com.huybq.fund_management.domain.late;

import com.huybq.fund_management.domain.pen_bill.PenBill;
import com.huybq.fund_management.domain.pen_bill.PenBillRepository;
import com.huybq.fund_management.domain.penalty.Penalty;
import com.huybq.fund_management.domain.penalty.PenaltyRepository;
import com.huybq.fund_management.domain.schedule.Schedule;
import com.huybq.fund_management.domain.schedule.ScheduleRepository;
import com.huybq.fund_management.domain.schedule.ScheduleService;
import com.huybq.fund_management.domain.team.Team;
import com.huybq.fund_management.domain.team.TeamService;
import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import com.huybq.fund_management.utils.chatops.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LateService {
    private final LateRepository repository;
    private final UserRepository userRepository;
    private final PenaltyRepository penaltyRepository;
    private final PenBillRepository penBillRepository;
    private final LateMapper mapper;
    private final Notification notification;
    private final TeamService teamService;
    private final ScheduleRepository scheduleRepository;
    //    private static final String API_URL = "https://chat.runsystem.vn/mqi9zi75fbdyxrowjirgz4h78r/posts?since=";
    //    private static final String TOKEN = "xxxxxxxxxxxxxxxx";


    public List<LateReponseDTO> getLateByUserIdWithDateRange(Long userId, LocalDate fromDate, LocalDate toDate) {
        return repository.findLatesByUser_IdAndDateRange(fromDate, toDate, userId).stream().map(mapper::toDTO).toList();
    }

    public void fetchLateCheckins(LocalTime time,String channelId) {
        Team team = teamService.getTeamBySlug("java");

        if (channelId==null){
            Schedule schedule = scheduleRepository.findByType(Schedule.NotificationType.valueOf("late_notification".toUpperCase()))
                    .orElseThrow(()->new ResourceNotFoundException("Schedule late_notification not found"));
            channelId = schedule.getChannelId();
        }

        LocalDateTime now = LocalDateTime.now();
        String todayString = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
        long timestamp = now.atZone(vietnamZone)
                .withHour(time.getHour()).withMinute(time.getMinute()).withSecond(time.getSecond())
                .toEpochSecond() * 1000;

        String url = "https://chat.runsystem.vn/api/v4/channels/" + channelId+ "/posts?since=" + timestamp;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + team.getToken());

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            Map<String, Object> posts = (Map<String, Object>) responseBody.get("posts");

            if (posts != null && !posts.isEmpty()) {
                // Lọc thông báo của ngày hiện tại
                List<String> matchedMessages = posts.values().stream()
                        .map(post -> (String) ((Map<String, Object>) post).get("message"))
                        .filter(message -> message != null && message.contains("THÔNG BÁO DANH SÁCH ĐI LÀM MUỘN " + todayString))
                        .collect(Collectors.toList());

                if (!matchedMessages.isEmpty()) {
                    matchedMessages.forEach(this::saveLateRecords);
                } else {
                    System.out.println("Không có message đi trễ nào trong dữ liệu API.");
                }
            } else {
                System.out.println("Không có bài viết nào trong dữ liệu API.");
            }
        } else {
            throw new RuntimeException("Lỗi khi gọi API: " + response.getStatusCode());
        }
    }

    //len lich goi tu dong tu 10h05 t2- t6
//        @Scheduled(cron = "*/10 * * * * ?", zone = "Asia/Ho_Chi_Minh")
//        @Scheduled(cron = "30 4 10 * * MON-FRI", zone = "Asia/Ho_Chi_Minh")
//        public void scheduledCheckinLate() {
//            DayOfWeek today = LocalDateTime.now().getDayOfWeek();
//            if (today != DayOfWeek.SATURDAY && today != DayOfWeek.SUNDAY) {
//                System.out.println("calling api check in late at 10:05...");
//                fetchLateCheckins();
//            }
//        }

    @Transactional
    public List<LateDTO> getUsersWithMultipleLatesInMonth() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        // Lấy danh sách user có số lần đi trễ > minLateCount
        List<Object[]> results = repository.findUsersWithLateCountInMonth(month, year, 1);

        // Lấy thông tin Penalty cho việc đi trễ
        Penalty penalty = penaltyRepository.findBySlug("late-check-in")
                .orElseThrow(() -> new ResourceNotFoundException("Penalty 'late-check-in' not found"));

        List<LateDTO> lateUsers = new ArrayList<>();

        for (Object[] result : results) {
            User user = (User) result[0];
            int lateCount = ((Number) result[1]).intValue();

            lateUsers.add(new LateDTO(user, lateCount));

            PenBill penBill = new PenBill();
            penBill.setUser(user);
            penBill.setPenalty(penalty);
            penBill.setDueDate(now.plusDays(7)); // Hạn nộp phạt sau 7 ngày
            penBill.setPaymentStatus(PenBill.Status.UNPAID);
            penBill.setDescription("Phiếu phạt do đi trễ quá số lần quy định trong tháng " + month + "/" + year);

            penBillRepository.save(penBill);
        }

        return lateUsers;
    }

//    @Scheduled(cron = "0 0 0 1 * ?", zone = "Asia/Ho_Chi_Minh")
//        @Scheduled(cron = "*/10 * * * * ?", zone = "Asia/Ho_Chi_Minh")
    public void processLatePenalties() {
        List<LateDTO> lateUsers = getUsersWithMultipleLatesInMonth();
        System.out.println("Đã xử lý phiếu phạt cho " + lateUsers.size() + " nhân sự đi trễ.");
    }

    /**
     * Trích xuất danh sách đi trễ từ message
     */
    public List<Late> parseLateRecords(String message) {
        List<Late> lateRecords = new ArrayList<>();

        Pattern datePattern = Pattern.compile("THÔNG BÁO DANH SÁCH ĐI LÀM MUỘN (\\d{4}/\\d{2}/\\d{2})");
        Matcher dateMatcher = datePattern.matcher(message);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        LocalDate reportDate = dateMatcher.find() ? LocalDate.parse(dateMatcher.group(1), formatter) : LocalDate.now();

        Pattern tablePattern = Pattern.compile("\\|(.*?)\\|\\s(.*?)\\|");
        Matcher tableMatcher = tablePattern.matcher(message);

        while (tableMatcher.find()) {
            String name = tableMatcher.group(1).trim();
            String checkinAt = tableMatcher.group(2).trim();

            if (name.equalsIgnoreCase("NAME") || checkinAt.equalsIgnoreCase("CHECKIN AT")) {
                continue;
            }

            String note = "";
            if (checkinAt.contains("(Có đơn NP)")
                    || checkinAt.equalsIgnoreCase("Nghỉ phép")) {
                continue;
            }

            Optional<User> userOpt = userRepository.findByFullName(name);
            if (userOpt.isEmpty()) {
                System.out.println("Not found user with name: " + name);
                continue;
            }
            User user = userOpt.get();

            LocalTime checkinTime = parseTime(checkinAt);

            Late late = Late.builder()
                    .user(user)
                    .date(reportDate)
                    .checkinAt(checkinTime)
                    .note(note)
                    .build();

            lateRecords.add(late);
        }

        return lateRecords;
    }
    /*
     * save records into db after parsing from message
     * */

    @Transactional
    public void saveLateRecords(String message) {
        List<Late> lateData = parseLateRecords(message);

        repository.deleteByDate(lateData.get(0).getDate());
        repository.saveAll(lateData);
        processLatePenalties();
        System.out.println("saving successfully.");
    }

    @Transactional(readOnly = true)
    public List<Late> getLateRecordsByDateRange(LocalDate fromDate, LocalDate toDate) {
        return repository.findByDateRange(fromDate, toDate);
    }

    /**
     * Parse thời gian từ string
     */
    private LocalTime parseTime(String time) {
        if (time == null || time.isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(time);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public List<Object[]> getLatesFromPrevious1stToCurrent1st() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusMonths(1).withDayOfMonth(28); // 28 tháng trước
        LocalDate endDate = today.withDayOfMonth(28); // 28 tháng này
        return repository.findUsersWithLateCountBetweenDates(startDate, endDate);
    }

    //            @Scheduled(cron = "0 0 8 1 * ?",zone = "Asia/Ho_Chi_Minh")// Chạy vào 08:00 ngày 1 mỗi tháng
    //    @Scheduled(cron = "0 12 14 26 * ?",zone = "Asia/Ho_Chi_Minh")// Chạy vào 08:00 ngày 28 mỗi tháng
    //@Scheduled(cron = "*/10 * * * * ?", zone = "Asia/Ho_Chi_Minh")
    public void sendLateReminder() {
        LocalDate today = LocalDate.now();

        List<Object[]> lateRecords = getLatesFromPrevious1stToCurrent1st();
        int previousMonth = today.getMonthValue() - 1;
        int currentMonth = today.getMonthValue();

        if (lateRecords.isEmpty()) {
            notification.sendNotification("@all\n🎉 **Tháng này không ai đi trễ!** 🎉", "java");
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("@all\n 🚨 **Danh sách đi trễ tháng ").append(previousMonth).append(" ** 🚨\n\n");
        message.append("| STT | TÊN | SỐ LẦN ĐI TRỄ |\n");
        message.append("|---|---|---|\n");

        int index = 1;
        for (Object[] record : lateRecords) {
            User user = (User) record[0];
            Long lateCount = (Long) record[1];

            message.append("| ").append(index++).append(" | ")
                    .append(user.getFullName()).append(" | ")
                    .append(lateCount).append(" |\n");

        }

        message.append("\nRất mong mọi người sẽ tuân thủ quy định và đến đúng giờ!\n")
                .append("Hãy cùng nhau xây dựng môi trường làm việc chuyên nghiệp nhé 💪🏻\n")
                .append("Trân trọng! \n\n")
                .append(" #checkin-statistic ");

        // Gửi thông báo lên ChatOps
        notification.sendNotification(message.toString(), "java");
    }
}
