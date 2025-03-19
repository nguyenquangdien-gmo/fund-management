package com.huybq.fund_management.domain.penalty;

import org.springframework.stereotype.Service;

@Service
public class PenaltyMapper {
    public PenaltyDTO toDTO(Penalty penalty) {
        return PenaltyDTO.builder()
                .id(penalty.getId())
                .name(penalty.getName())
                .amount(penalty.getAmount())
                .description(penalty.getDescription())
                .createdAt(penalty.getCreatedAt())
                .build();
    }

    public Penalty toEntity(PenaltyDTO dto) {
        return Penalty.builder()
                .name(dto.getName())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .build();
    }
}
