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
                .date(work.getDate())
                .type(work.getType())
                .timePeriod(work.getTimePeriod())
                .reason(work.getReason())
                .status(work.getStatus().name())
                .approvedByName(work.getApprovedBy()==null?null:work.getApprovedBy().getFullName())
                .createdAt(work.getCreatedAt())
                .build();
    }

    public Work toWork(WorkDTO request) {
        return Work.builder()
                .date(request.getDate())
                .type(request.getType())
                .timePeriod(TimePeriod.valueOf(request.getTimePeriod()))
                .reason(request.getReason())
                .build();
    }
}
