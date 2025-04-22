package com.huybq.fund_management.domain.work;

import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.exception.BusinessException;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkService {
    private final WorkRepository workRepository;
    private final UserRepository userRepository;
    private final WorkMapper mapper;

    @Transactional
    public WorkResponseDTO createWork(WorkDTO request) {
        // Validate input
        if (request.getDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Cannot create work status for past dates");
        }

        // Check if user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        // Check for existing entries for the same date and period
        workRepository.findByUserIdAndDateAndTimePeriod(
                        request.getUserId(), request.getDate(), TimePeriod.valueOf(request.getTimePeriod()))
                .ifPresent(existingStatus -> {
                    throw new BusinessException("Work status already exists for this date and period");
                });

        // Create and save work status
        Work work = new Work();
        work.setUser(user);
        work.setStatus(Work.Status.PENDING);
        work.setDate(request.getDate());
        work.setType(request.getType());
        work.setTimePeriod(TimePeriod.valueOf(request.getTimePeriod()));
        work.setReason(request.getReason());

        Work savedStatus = workRepository.save(work);

        return mapper.toWorkResponseDTO(savedStatus);
    }

    public List<WorkResponseDTO> getWorksByUserId(Long userId) {
        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Work> works = workRepository.findByUserId(userId);
        return works.stream()
                .map(mapper::toWorkResponseDTO)
                .collect(Collectors.toList());
    }

    public List<WorkResponseDTO> getWorksByUserIdAndMonth(Long userId, int year, int month) {
        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Work> works = workRepository.findByUserIdAndMonth(userId, year, month);
        return works.stream()
                .map(mapper::toWorkResponseDTO)
                .collect(Collectors.toList());
    }

    public List<WorkResponseDTO> getWorksByDate(LocalDate date) {
        List<Work> works = workRepository.findByDate(date);
        return works.stream()
                .map(mapper::toWorkResponseDTO)
                .collect(Collectors.toList());
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
    public WorkResponseDTO rejectWork(Long statusId, Long rejectorId) {
        Work work = workRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Work status not found with id: " + statusId));

        User approver = userRepository.findById(rejectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found with id: " + rejectorId));

        work.setStatus(Work.Status.REJECTED);
        work.setApprovedBy(approver);

        Work updatedStatus = workRepository.save(work);
        return mapper.toWorkResponseDTO(updatedStatus);
    }

    @Transactional
    public void deleteWorkStatus(Long statusId) {
        Work work = workRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Work status not found with id: " + statusId));

        if (work.getStatus() == Work.Status.APPROVED) {
            throw new BusinessException("Cannot delete an approved work status");
        }

        workRepository.delete(work);
    }

    public Long countWorkDaysInMonthWithType(Long userId,String type, int year, int month) {
        return workRepository.countByUserIdAndTypeAndMonth(userId, StatusType.valueOf(type), year, month);
    }

}
