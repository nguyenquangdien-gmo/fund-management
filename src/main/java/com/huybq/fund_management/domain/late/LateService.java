package com.huybq.fund_management.domain.late;

import com.huybq.fund_management.domain.pen_bill.PenBill;
import com.huybq.fund_management.domain.pen_bill.PenBillDTO;
import com.huybq.fund_management.domain.pen_bill.PenBillRepository;
import com.huybq.fund_management.domain.pen_bill.PenBillService;
import com.huybq.fund_management.domain.penalty.Penalty;
import com.huybq.fund_management.domain.penalty.PenaltyRepository;
import com.huybq.fund_management.domain.schedule.Schedule;
import com.huybq.fund_management.domain.schedule.ScheduleRepository;
import com.huybq.fund_management.domain.team.Team;
import com.huybq.fund_management.domain.team.TeamService;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserMapper;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.domain.user.UserResponseDTO;
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
    private final Notification notification;
    private final TeamService teamService;
    private final PenBillService penBillService;
    private final ScheduleRepository scheduleRepository;
    private final LateMapper mapper;
    private final UserMapper userMapper;

    public List<LateResponseDTO> getLateByUserIdWithDateRange(Long userId, LocalDate fromDate, LocalDate toDate) {
        return repository.findLatesByUser_IdAndDateRange(fromDate, toDate, userId).stream().map(mapper::toReponseDTO).toList();
    }

    public List<UserLateCountDTO> getAllUserLateCountsInMonth(int month, int year) {
        List<Object[]> results = repository.countLatesByUserInMonthAndYear(month, year);

        return results.stream()
                .map(row -> new UserLateCountDTO(
                        (Long) row[0],
                        (String) row[1],
                        ((Number) row[2]).intValue()
                ))
                .collect(Collectors.toList());
    }

    public void fetchLateCheckins(LocalTime time, String channelId) {
        if (channelId == null) {
            throw new IllegalArgumentException("Channel ID is null");
        }

        Team team = teamService.getTeamBySlug("java");
        if (team == null || team.getToken() == null) {
            throw new IllegalStateException("Team or team token is null");
        }

        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime now = LocalDateTime.now();
        String todayString = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        LocalTime targetTime = Optional.ofNullable(time).orElse(LocalTime.of(10, 0, 0));
        long timestamp = now.toLocalDate()
                .atTime(targetTime)
                .atZone(vietnamZone)
                .toEpochSecond() * 1000;

        String url = String.format("https://chat.runsystem.vn/api/v4/channels/%s/posts?since=%d", channelId, timestamp);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(team.getToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Lỗi khi gọi API: " + response.getStatusCode());
        }

        Map<String, Object> posts = (Map<String, Object>) response.getBody().get("posts");
        if (posts == null || posts.isEmpty()) {
            System.out.println("Không có bài viết nào trong dữ liệu API.");
            return;
        }

        List<String> matchedMessages = posts.values().stream()
                .map(post -> (String) ((Map<String, Object>) post).get("message"))
                .filter(message -> message != null && message.contains("THÔNG BÁO DANH SÁCH ĐI LÀM MUỘN " + todayString))
                .collect(Collectors.toList());

        if (matchedMessages.isEmpty()) {
            System.out.println("Không có message đi trễ nào trong dữ liệu API.");
            return;
        }

        matchedMessages.forEach(this::saveLateRecords);
    }

    public void fetchLateCheckinsForCheckNow(LocalTime time, String channelId) {
        Team team = teamService.getTeamBySlug("java");
        // Kiểm tra sự tồn tại của schedule trước khi lấy channelId
        Optional<Schedule> scheduleOpt = scheduleRepository.findByType(Schedule.NotificationType.valueOf("LATE_NOTIFICATION"));

        if (scheduleOpt.isPresent()) {
            // Lấy channelId từ schedule nếu có
            channelId = scheduleOpt.get().getChannelId().toString();
        } else if (channelId == null || "default-channel-id".equals(channelId)) {
            // Nếu không có schedule và channelId vẫn null, ném lỗi
            throw new ResourceNotFoundException("Schedule 'late-check-in' not found or channelId is null");
        }

        LocalDateTime now = LocalDateTime.now();
        String todayString = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
        long timestamp = now.atZone(vietnamZone)
                .withHour(time.getHour()).withMinute(time.getMinute()).withSecond(time.getSecond())
                .toEpochSecond() * 1000;

        String url = "https://chat.runsystem.vn/api/v4/channels/" + channelId + "/posts?since=" + timestamp;

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

    //len lich goi tu dong tu 10h t2- t6
    @Scheduled(cron = "0 5 10 * * MON-FRI", zone = "Asia/Ho_Chi_Minh")
    public void scheduledCheckinLate() {
        Schedule schedule = scheduleRepository.findByType(Schedule.NotificationType.valueOf("LATE_NOTIFICATION"))
                .orElseThrow(() -> new ResourceNotFoundException("Schedule 'late-check-in' not found"));
        fetchLateCheckins(null, schedule.getChannelId());
    }

    @Transactional
    public int processUserWithMultipleLatesInDate() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        List<User> lateUsers = repository.findUserLateInDate(today);

        // Lấy thông tin Penalty cho việc đi trễ
        Penalty penalty = penaltyRepository.findBySlug("late-check-in")
                .orElseThrow(() -> new ResourceNotFoundException("Penalty 'late-check-in' not found"));

        for (User user : lateUsers) {

            PenBill penBill = new PenBill();
            penBill.setUser(user);
            penBill.setPenalty(penalty);
            penBill.setDueDate(today.plusDays(7)); // Hạn nộp phạt sau 7 ngày
            penBill.setPaymentStatus(PenBill.Status.UNPAID);
            penBill.setDescription("Phạt do đi trễ quá số lần quy định trong tháng " + month + "/" + year);

            penBillRepository.save(penBill);
        }

        return lateUsers.size();
    }

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

    @Transactional
    public void saveLateRecords(String message) {
        List<Late> lateData = parseLateRecords(message);
        repository.deleteByDate(lateData.get(0).getDate());
        repository.flush();
        repository.saveAll(lateData);

        int size = processUserWithMultipleLatesInDate();
        System.out.println("Đã xử lý phiếu phạt cho " + size + " nhân sự đi trễ.");
        System.out.println("saving successfully.");
    }

//    @Transactional(readOnly = true)
//    public List<Late> getLateRecordsByDateRange(LocalDate fromDate, LocalDate toDate) {
//        return repository.findByDateRange(fromDate, toDate);
//    }

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

//    public static String formatLocalDate(LocalDate date) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//        return date.format(formatter);
//    }
//
//    public List<UserResponseDTO> getUsersWithLateDate() {
//        LocalDate today = LocalDate.now();
//        return repository.findUsersWithLateInDate(today).stream()
//                .map(userMapper::toResponseDTO).toList();
//    }

    @Transactional(readOnly = true)
    public List<LateWithPenBillDTO> getLateRecordsWithPenBill(LocalDate fromDate, LocalDate toDate) {
        // Lấy tất cả các bản ghi Late trong khoảng thời gian từ fromDate đến toDate
        List<Late> lates = repository.findByDateRange(fromDate, toDate);
        List<LateWithPenBillDTO> result = new ArrayList<>();

        for (Late late : lates) {
            Optional<PenBillDTO> penBillOpt = penBillService.findByUserAndPenaltyAndDate(
                    late.getUser(), "late-check-in", late.getDate()); // Giả sử penaltyID là 1L

            LateWithPenBillDTO lateWithPenBillDTO = new LateWithPenBillDTO(
                    late.getId(),
                    userMapper.toResponseDTO(late.getUser()),
                    late.getDate(),
                    late.getCheckinAt(),
                    late.getNote(),
                    penBillOpt.orElse(null)
            );

            // Thêm LateWithPenBillDTO vào danh sách kết quả
            result.add(lateWithPenBillDTO);
        }

        return result;
    }

    @Transactional
    public void deleteLateRecord(Long lateId, Long penBillId) {
        // Xóa bản ghi PenBill nếu có
        if (penBillId != null) {
            penBillRepository.deleteById(penBillId);
        }

        // Xóa bản ghi đi muộn
        repository.deleteById(lateId);
    }

}
