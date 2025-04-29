package com.huybq.fund_management.domain.work;

import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.exception.BusinessException;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkService {
    private final WorkRepository workRepository;
    private final UserRepository userRepository;
    private final WorkMapper mapper;

    public List<WorkResponseDTO> getAllWorks() {
        List<Work> works = workRepository.findAll();
        return works.stream()
                .map(mapper::toWorkResponseDTO)
                .collect(Collectors.toList());
    }

    public List<WorkResponseDTO> getWorksByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Work> works = workRepository.findByUserId(userId);
        return works.stream()
                .map(mapper::toWorkResponseDTO)
                .collect(Collectors.toList());
    }

    public List<WorkResponseDTO> createWork(WorkDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Không cho tạo nếu là tháng trước
        LocalDate now = LocalDate.now();
        if (dto.getFromDate().getMonthValue() < now.getMonthValue() || dto.getEndDate().getMonthValue() < now.getMonthValue()) {
            throw new BusinessException("Cannot create work in the previous month");
        }

        // Kiểm tra xem đã tồn tại Work giao thời gian chưa
        boolean exists = workRepository.existsByUserIdAndDateRangeOverlap(
                dto.getUserId(), dto.getFromDate(), dto.getEndDate()
        );
        if (exists) {
            throw new BusinessException("This user already has work during the selected time period");
        }

        // Xử lý thời gian bắt đầu - kết thúc
        LocalTime start = dto.getStartTime() != null ? dto.getStartTime() : LocalTime.of(8, 0);
        LocalTime end = dto.getEndTime() != null ? dto.getEndTime() : LocalTime.of(17, 0);
        TimePeriod period = resolveTimePeriod(start, end);

        // Tạo và lưu Work
        Work work = new Work();
        work.setUser(user);
        work.setFromDate(dto.getFromDate());
        work.setEndDate(dto.getEndDate());
        work.setStartTime(start);
        work.setEndTime(end);
        work.setType(dto.getType());
        work.setTimePeriod(period);
        work.setReason(dto.getReason());
        work.setIdCreate(dto.getIdCreate());

        Work saved = workRepository.save(work);
        return List.of(mapper.toWorkResponseDTO(saved));
    }


    private TimePeriod resolveTimePeriod(LocalTime start, LocalTime end) {
        if (start.equals(LocalTime.of(8, 0)) && end.equals(LocalTime.of(12, 0))) {
            return TimePeriod.AM;
        } else if (start.equals(LocalTime.of(12, 0)) && end.equals(LocalTime.of(17, 0))) {
            return TimePeriod.PM;
        } else if (start.equals(LocalTime.of(8, 0)) && end.equals(LocalTime.of(17, 0))) {
            return TimePeriod.FULL;
        }
        return TimePeriod.FULL; // fallback
    }

    public List<UserWorkResponse> getWorksByDate(LocalDate date) {
        List<Work> works = workRepository.findByDate(date);

        return works.stream()
                .map(w -> UserWorkResponse.builder()
                        .userId(w.getUser().getId())
                        .fullName(w.getUser().getFullName())
                        .type(w.getType().name())
                        .startTime(w.getStartTime())
                        .endTime(w.getEndTime())
                        .build())
                .collect(Collectors.toList());
    }

    public int countDaysInMonthWithType(Long userId, int year, int month, String type) {
        List<Work> works = workRepository.findWorksByUserAndMonthWithType(userId, year, month, StatusType.valueOf(type));

        int totalDays = 0;
        for (Work work : works) {
            // For each work record, calculate days between fromDate and endDate (inclusive)
            // Adding 1 because both start and end dates should be counted
            long days = ChronoUnit.DAYS.between(work.getFromDate(), work.getEndDate()) + 1;
            totalDays += days;
        }

        return totalDays;
    }

    public List<WorkSummaryResponse> getWorkSummaryByMonth(int year, int month) {
        List<User> users = userRepository.findAllByIsDeleteIsFalse();

        return users.stream().map(user -> {
            List<Work> works = workRepository.findWorksByUserAndMonth(user.getId(), year, month);

            return new WorkSummaryResponse(user.getId(), user.getFullName(), works.stream().map(mapper::toWorkStatsResponse).toList());
        }).collect(Collectors.toList());
    }

    private long daysBetween(LocalDate from, LocalDate to) {
        return ChronoUnit.DAYS.between(from, to) + 1;
    }

    public List<WorkResponseDTO> getUserWorkDetails(Long userId, int year, int month) {
        return workRepository.findWorksByUserAndMonth(userId, year, month)
                .stream()
                .map(mapper::toWorkResponseDTO)
                .collect(Collectors.toList());
    }

    public List<UserWorkResponse> getWorkByDate(LocalDate date) {
        List<Work> works = workRepository.findByDate(date);
        return works.stream()
                .map(w -> UserWorkResponse.builder()
                        .userId(w.getUser().getId())
                        .fullName(w.getUser().getFullName())
                        .fromDate(w.getFromDate())
                        .toDate(w.getEndDate())
                        .startTime(w.getStartTime())
                        .endTime(w.getEndTime())
                        .type(w.getType().name())
                        .build())
                .collect(Collectors.toList());
    }

//    public List<WorkResponseDTO> getWorksByDate(LocalDate date) {
//        List<Work> works = workRepository.findByDateRange(date);
//        return works.stream()
//                .map(mapper::toWorkResponseDTO)
//                .collect(Collectors.toList());
//    }

    @Transactional
    public void deleteWork(Long id) {
        workRepository.delete(workRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work status not found with id: " + id)));
    }
}
