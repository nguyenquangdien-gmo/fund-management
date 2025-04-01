package com.huybq.fund_management.domain.user.mapper;

import com.huybq.fund_management.domain.user.dto.UserDto;
import com.huybq.fund_management.domain.user.entity.User;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {
    public UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .position(user.getPosition())
                .team(user.getTeam().getName())
                .phoneNumber(user.getPhone())
                .dob(user.getDob().toString())
                .build();
    }
}
