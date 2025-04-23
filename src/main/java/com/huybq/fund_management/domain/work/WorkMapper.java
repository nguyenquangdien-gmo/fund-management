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
                .fromDate(work.getFromDate())
                .toDate(work.getToDate())
                .type(work.getType())
                .timePeriod(work.getTimePeriod())
                .reason(work.getReason())
                .status(work.getStatus().name())
                .approvedByName(work.getApprovedBy()==null?null:work.getApprovedBy().getFullName())
                .approvedById(work.getApprovedBy()==null?null:work.getApprovedBy().getId())
                .createdAt(work.getCreatedAt())
                .build();
    }

    public Work toWork(WorkDTO request) {
        return Work.builder()
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .type(request.getType())
                .timePeriod(TimePeriod.valueOf(request.getTimePeriod()))
                .reason(request.getReason())
                .build();
    }
}
