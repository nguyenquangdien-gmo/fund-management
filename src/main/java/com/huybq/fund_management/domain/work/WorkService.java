package com.huybq.fund_management.domain.work;

import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.exception.BusinessException;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

        LocalDate fromDate = dto.getFromDate(); // assume fromDate, toDate in DTO
        LocalDate toDate = dto.getToDate();

        List<Work> works = new ArrayList<>();

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            LocalTime start = dto.getStartTime() != null
                    ? dto.getStartTime()
                    : LocalTime.of(8, 0);

            LocalTime end = dto.getEndTime() != null
                    ? dto.getEndTime()
                    : LocalTime.of(17, 0);

            TimePeriod period = resolveTimePeriod(start, end);

            Work work = new Work();
            work.setUser(user);
            work.setDate(date);
            work.setStartTime(start);
            work.setEndTime(end);
            work.setType(dto.getType());
            work.setTimePeriod(period);
            work.setReason(dto.getReason());

            works.add(work);
        }

        List<Work> saved = workRepository.saveAll(works);
        return saved.stream()
                .map(mapper::toWorkResponseDTO)
                .collect(Collectors.toList());
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

    public Long countDaysInMonthWithType(Long userId, int year, int month, String type) {
        return workRepository.findWFHByUserAndMonth(userId, month, year, StatusType.valueOf(type))
                .stream().count(); // Mỗi work đại diện 1 ngày
    }

    public List<WorkSummaryResponse> getWorkSummaryByMonth(int year, int month) {
        List<User> users = userRepository.findAllByIsDeleteIsFalse();

        return users.stream().map(user -> {
            List<Work> works = workRepository.findWorksByUserAndMonth(user.getId(), year, month);

            long wfhDays = works.stream()
                    .filter(w -> w.getType() == StatusType.WFH)
                    .count();

            long leaveDays = works.stream()
                    .filter(w -> w.getType() == StatusType.LEAVE)
                    .count();

            return new WorkSummaryResponse(user.getId(), user.getFullName(), wfhDays, leaveDays);
        }).collect(Collectors.toList());
    }

    public List<WorkResponseDTO> getUserWorkDetails(Long userId, int year, int month) {
        return workRepository.findByUserIdAndMonthAndYear(userId, month, year)
                .stream()
                .map(mapper::toWorkResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateWork(Long id, WorkUpdateDTO dto) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work not found with id: " + id));

        if (dto.getDate() != null) {
            work.setDate(dto.getDate());
        }

        if (dto.getStartTime() != null) {
            work.setStartTime(dto.getStartTime());
        }

        if (dto.getEndTime() != null) {
            work.setEndTime(dto.getEndTime());
        }

        if (dto.getType() != null) {
            work.setType(dto.getType());
        }

        if (dto.getReason() != null) {
            work.setReason(dto.getReason());
        }

        // Tính lại TimePeriod nếu thời gian thay đổi
        if (dto.getStartTime() != null || dto.getEndTime() != null) {
            TimePeriod period = resolveTimePeriod(
                    work.getStartTime(),
                    work.getEndTime()
            );
            work.setTimePeriod(period);
        }

        workRepository.save(work);
    }

    @Transactional
    public void deleteWork(Long id) {
        workRepository.delete(workRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work status not found with id: " + id)));
    }
}
