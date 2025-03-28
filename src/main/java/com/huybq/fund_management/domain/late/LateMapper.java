package com.huybq.fund_management.domain.late;

import org.springframework.stereotype.Service;

@Service
public class LateMapper {
    public LateReponseDTO toDTO(Late late) {
        return LateReponseDTO.builder()
                .id(late.getId())
                .user(late.getUser())
                .date(late.getDate())
                .checkinAt(late.getCheckinAt())
                .note(late.getNote())
                .build();
    }
}
