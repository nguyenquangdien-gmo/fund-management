package com.huybq.fund_management.domain.late;

import com.huybq.fund_management.domain.pen_bill.PenBill;
import com.huybq.fund_management.domain.pen_bill.PenBillRepository;
import com.huybq.fund_management.domain.penalty.Penalty;
import com.huybq.fund_management.domain.penalty.PenaltyRepository;
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

@Service
@RequiredArgsConstructor
public class LateService {
    private final LateRepository repository;
    private final UserRepository userRepository;
    private final PenaltyRepository penaltyRepository;
    private final PenBillRepository penBillRepository;
    private final Notification notification;
    private static final String API_URL = "https://dev/mqi9zi75fbdyxrowjirgz4h78r/posts?since=";
    private static final String TOKEN = "xxxxxxxxxxxxxxxx";

    public void fetchLateCheckins() {
        LocalDateTime now = LocalDateTime.now();
        long timestamp = now.withHour(10).withMinute(0).withSecond(0).toEpochSecond(ZoneOffset.UTC) * 1000;
        String url = API_URL + timestamp;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + TOKEN);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            Map<String, Object> posts = (Map<String, Object>) responseBody.get("posts");

            if (posts != null && !posts.isEmpty()) {
                List<String> order = (List<String>) responseBody.get("order");
                if (order != null && !order.isEmpty()) {
                    String postId = order.get(0);
                    Map<String, Object> postData = (Map<String, Object>) posts.get(postId);

                    if (postData != null) {
                        String message = (String) postData.get("message");
                        if (message != null && !message.isEmpty()) {
                            saveLateRecords(message);
                        } else {
                            System.out.println("Không có message trong bài viết.");
                        }
                    }
                }
            } else {
                System.out.println("Không có bài viết nào trong dữ liệu API.");
            }
        } else {
            throw new RuntimeException("Lỗi khi gọi API: " + response.getStatusCode());
        }
    }

    //len lich goi tu dong tu 10h05 t2- t6
    @Scheduled(cron = "0 5 10 * * MON-FRI", zone = "Asia/Ho_Chi_Minh") // Chạy lúc 10:05:00 từ Thứ 2 đến Thứ 6
    public void scheduledCheckinLate() {
        DayOfWeek today = LocalDateTime.now().getDayOfWeek();
        if (today != DayOfWeek.SATURDAY && today != DayOfWeek.SUNDAY) {
            System.out.println("calling api check in late at 10:05...");
            fetchLateCheckins();
        }
    }

    @Transactional
    public List<LateDTO> getUsersWithMultipleLatesInMonth(int minLateCount) {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        // Lấy danh sách user có số lần đi trễ >= minLateCount
        List<Object[]> results = repository.findUsersWithLateCountInMonth(month, year, minLateCount);

        // Lấy thông tin Penalty cho việc đi trễ
        Penalty penalty = penaltyRepository.findBySlug("late-check-in")
                .orElseThrow(() -> new ResourceNotFoundException("Penalty 'late-check-in' not found"));

        List<LateDTO> lateUsers = new ArrayList<>();

        for (Object[] result : results) {
            User user = (User) result[0];
            int lateCount = ((Number) result[1]).intValue();

            lateUsers.add(new LateDTO(user, lateCount));

            // Tính số lượng phiếu phạt cần tạo (mỗi lần đi trễ sau minLateCount đều bị phạt)
            int penaltyCount = lateCount - minLateCount;

            for (int i = 0; i < penaltyCount; i++) {
                PenBill penBill = new PenBill();
                penBill.setUser(user);
                penBill.setPenalty(penalty);
                penBill.setDueDate(now.plusDays(7)); // Hạn nộp phạt sau 7 ngày
                penBill.setPaymentStatus(PenBill.Status.PENDING);
                penBill.setDescription("Phiếu phạt lần " + (i + 1) + " do đi trễ quá số lần quy định trong tháng " + month + "/" + year);

                penBillRepository.save(penBill);
            }
        }

        return lateUsers;
    }

        @Scheduled(cron = "0 0 0 28 * ?", zone = "Asia/Ho_Chi_Minh")
//    @Scheduled(cron = "*/10 * * * * ?", zone = "Asia/Ho_Chi_Minh")
    public void processLatePenalties() {

        int minLateCount = 1;
        List<LateDTO> lateUsers = getUsersWithMultipleLatesInMonth(minLateCount);
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
            if (checkinAt.contains("(Có đơn NP)")) {
                note = "Có đơn NP";
                checkinAt = checkinAt.replaceAll("\\(Có đơn NP\\)", "").trim();
            }
            if (checkinAt.equalsIgnoreCase("Nghỉ phép") || checkinAt.equals("-") || checkinAt.isEmpty()) {
                note = "Nghỉ phép";
                checkinAt = null;
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

        if (lateData.isEmpty()) {
            notification.sendNotification("Thật tuyệt vời, hôm nay team không có ai đi trễ cả! :smile: ");
        }else {
            repository.deleteByDate(lateData.get(0).getDate());
            repository.saveAll(lateData);
            notification.sendNotification(message);
            System.out.println("saving successfully.");
        }
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

    public List<Object[]> getLatesFromPrevious28thToCurrent28th() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusMonths(1).withDayOfMonth(28); // 28 tháng trước
        LocalDate endDate = today.withDayOfMonth(28); // 28 tháng này
        return repository.findUsersWithLateCountBetweenDates(startDate, endDate);
    }

    @Scheduled(cron = "0 0 8 28 * ?",zone = "Asia/Ho_Chi_Minh")// Chạy vào 08:00 ngày 28 mỗi tháng
//    @Scheduled(cron = "0 12 14 26 * ?",zone = "Asia/Ho_Chi_Minh")// Chạy vào 08:00 ngày 28 mỗi tháng
    public void sendLateReminder() {
        LocalDate today = LocalDate.now();

        List<Object[]> lateRecords = getLatesFromPrevious28thToCurrent28th();
        int previousMonth = today.getMonthValue() - 1;
        int currentMonth = today.getMonthValue();

        if (lateRecords.isEmpty()) {
            notification.sendNotification("@all\n🎉 **Tháng này không ai đi trễ!** 🎉");
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("@all\n 🚨 **Danh sách đi trễ từ 28/").append(previousMonth).append(" đến 28/").append(currentMonth).append(" ** 🚨\n\n");
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
        notification.sendNotification(message.toString());
    }
}
