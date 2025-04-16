package com.huybq.fund_management.domain.late;

import com.huybq.fund_management.domain.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LateMapper {
    private final UserMapper userMapper;

    public LateResponseDTO toReponseDTO(Late late) {
        return LateResponseDTO.builder()
                .id(late.getId())
                .user(userMapper.toResponseDTO(late.getUser()))
                .date(late.getDate())
                .checkinAt(late.getCheckinAt())
                .note(late.getNote())
                .build();
    }
}
