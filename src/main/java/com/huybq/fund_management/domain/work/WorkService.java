package com.huybq.fund_management.domain.work;

import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.exception.BusinessException;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    public List<WorkResponseDTO> getWorksByUserIdAndMonth(Long userId, int year, int month) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<Work> works = workRepository.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        return works.stream()
                .map(mapper::toWorkResponseDTO)
                .collect(Collectors.toList());
    }

    public List<WorkSummaryResponse> getWorkSummaryByMonth(int year, int month) {
        List<User> users = userRepository.findAllByIsDeleteIsFalse();

        return users.stream().map(user -> {
            List<Work> works = workRepository.findByUserAndMonthAndYear(user.getId(), year, month);

            long wfhDays = works.stream()
                    .filter(w -> w.getType() == StatusType.WFH)
                    .mapToLong(w -> daysBetween(w.getFromDate(), w.getToDate()))
                    .sum();

            long leaveDays = works.stream()
                    .filter(w -> w.getType() == StatusType.LEAVE)
                    .mapToLong(w -> daysBetween(w.getFromDate(), w.getToDate()))
                    .sum();

            return new WorkSummaryResponse(user.getId(), user.getFullName(), wfhDays, leaveDays);
        }).collect(Collectors.toList());
    }

    private long daysBetween(LocalDate from, LocalDate to) {
        return ChronoUnit.DAYS.between(from, to) + 1;
    }

    public List<UserWorkResponse> getWorkByDate(LocalDate date) {
        List<Work> works = workRepository.findByDateInRange(date);
        return works.stream()
                .map(w -> UserWorkResponse.builder()
                        .userId(w.getUser().getId())
                        .fullName(w.getUser().getFullName())
                        .type(w.getType().name())
                        .build())
                .collect(Collectors.toList());
    }


    public List<WorkResponseDTO> getWorksByDate(LocalDate date) {
        List<Work> works = workRepository.findByDateRange(date);
        return works.stream()
                .map(mapper::toWorkResponseDTO)
                .collect(Collectors.toList());
    }

    public List<WorkResponseDTO> getWorksWithStatus(String status) {
        List<Work> pendingWorks = workRepository.findByStatus(Work.Status.valueOf(status));

        return pendingWorks.stream().map(mapper::toWorkResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public WorkResponseDTO createWork(WorkDTO request) {
        if (request.getFromDate().isBefore(LocalDate.now()) || request.getToDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Cannot create work status for past dates");
        }

        if (request.getFromDate().isAfter(request.getToDate())) {
            throw new BusinessException("From date must be before or equal to To date");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        List<Work> overlappingWorks = workRepository.findOverlappingWorks(
                request.getUserId(), request.getFromDate(), request.getToDate(),
                TimePeriod.valueOf(request.getTimePeriod()));

        if (!overlappingWorks.isEmpty()) {
            throw new BusinessException("Work status already exists in the selected date range and time period");
        }

        Work work = new Work();
        work.setUser(user);
        work.setStatus(Work.Status.PENDING);
        work.setFromDate(request.getFromDate());
        work.setToDate(request.getToDate());
        work.setType(request.getType());
        work.setTimePeriod(TimePeriod.valueOf(request.getTimePeriod()));
        work.setReason(request.getReason());

        Work savedStatus = workRepository.save(work);
        return mapper.toWorkResponseDTO(savedStatus);
    }

    public void updateWork(Long workId, WorkDTO dto) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new RuntimeException("Work not found with id " + workId));

        if (work.getStatus() != Work.Status.PENDING) {
            throw new IllegalStateException("Cannot update a work that is already approved or rejected.");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id " + dto.getUserId()));

        work.setUser(user);
        work.setFromDate(dto.getFromDate());
        work.setToDate(dto.getToDate());
        work.setType(dto.getType());
        work.setTimePeriod(TimePeriod.valueOf(dto.getTimePeriod())); // cần validate input trước khi gọi
        work.setReason(dto.getReason());

        workRepository.save(work);
    }

    @Transactional
    public WorkResponseDTO approveWork(Long id, Long approverId) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work status not found with id: " + id));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found with id: " + approverId));

        work.setStatus(Work.Status.APPROVED);
        work.setApprovedBy(approver);

        Work updatedStatus = workRepository.save(work);
        return mapper.toWorkResponseDTO(updatedStatus);
    }

    @Transactional
    public WorkResponseDTO rejectWork(Long id, Long rejectorId) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work status not found with id: " + id));

        User rejector = userRepository.findById(rejectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found with id: " + rejectorId));

        work.setStatus(Work.Status.REJECTED);
        work.setApprovedBy(rejector);

        Work updatedStatus = workRepository.save(work);
        return mapper.toWorkResponseDTO(updatedStatus);
    }

    @Transactional
    public void deleteWorkStatus(Long id) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work status not found with id: " + id));

        if (work.getStatus() == Work.Status.APPROVED) {
            throw new BusinessException("Cannot delete an approved work status");
        }

        workRepository.delete(work);
    }

    public Long countWorkDaysInMonthWithType(Long userId, String type, int year, int month) {
        List<Work> works = workRepository.findByUserIdAndType(userId, StatusType.valueOf(type));

        return works.stream()
                .flatMap(work -> work.getFromDate().datesUntil(work.getToDate().plusDays(1)))
                .filter(date -> date.getYear() == year && date.getMonthValue() == month)
                .count();
    }
}
