package com.huybq.fund_management.domain.late;

import com.huybq.fund_management.domain.team.TeamDTO;
import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LateService {
    private final LateRepository repository;
    private UserRepository userRepository;


    public TeamDTO createTeam(TeamDTO teamDTO) {
        return null;
    }

    public List<Map<String, String>> extractLateData(String message) {
        List<Map<String, String>> lateData = new ArrayList<>();
        String regex = "\\|([A-ZÀ-Ỹ][^|]+?)\\s*\\|\\s*([^|]+)\\s*\\|";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String name = matcher.group(1).trim();
            String checkinAt = matcher.group(2).trim();

            if (!name.equalsIgnoreCase("NAME") && !checkinAt.equals("-") && !checkinAt.equalsIgnoreCase("Nghỉ phép")) {
                Map<String, String> record = new HashMap<>();
                record.put("name", name);
                record.put("checkinAt", checkinAt.replaceAll("\\s*\\(.*\\)", "")); // Loại bỏ phần "(Có đơn NP)"
                lateData.add(record);
            }
        }
        return lateData;
    }

    public void saveLateRecords(String message) {
        List<Map<String, String>> lateData = extractLateData(message);
        LocalDate today = LocalDate.now();

        for (Map<String, String> data : lateData) {
            String name = data.get("name");
            LocalTime checkinTime = parseTime(data.get("checkinAt"));

            User user = userRepository.findByFullName(name)
                    .orElseThrow(() -> new EntityNotFoundException("User " + name + " not found in repository"));

            Late record = Late.builder()
                    .user(user)
                    .date(today)
                    .checkinAt(checkinTime)
                    .build();
            repository.save(record);
        }
    }

    private LocalTime parseTime(String time) {
        try {
            return LocalTime.parse(time);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Late> getLateRecordsByDate(LocalDate date) {
        return repository.findByDate(date);
    }
}
