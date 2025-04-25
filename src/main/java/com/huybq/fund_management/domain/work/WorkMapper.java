package com.huybq.fund_management.domain.work;

import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkMapper {
    public WorkResponseDTO toWorkResponseDTO(Work work) {
        return WorkResponseDTO.builder()
                .id(work.getId())
                .userId(work.getUser().getId())
                .fullName(work.getUser().getFullName())
                .startTime(work.getStartTime())
                .endTime(work.getEndTime())
                .fromDate(work.getFromDate())
                .endDate(work.getEndDate())
                .type(work.getType())
                .timePeriod(work.getTimePeriod())
                .reason(work.getReason())
                .approvedByName(work.getApprovedBy() == null ? null : work.getApprovedBy().getFullName())
                .approvedById(work.getApprovedBy() == null ? null : work.getApprovedBy().getId())
                .createdAt(work.getCreatedAt())
                .idCreate(work.getIdCreate())
                .build();
    }
}
