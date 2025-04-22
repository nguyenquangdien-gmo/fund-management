package com.huybq.fund_management.domain.user;

import org.springframework.context.support.BeanDefinitionDsl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserMapper {
    public UserDTO toDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .position(user.getPosition())
                .slugTeam(user.getTeam().getName())
                .phoneNumber(user.getPhone())
                .dob(user.getDob().toString())
                .joinDate(user.getJoinDate().toString())
                .userIdChat(user.getUserIdChat())
                .build();
    }

    public UserResponseDTO toResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .position(user.getPosition())
                .team(user.getTeam().getName())
                .phoneNumber(user.getPhone())
                .dob(user.getDob().toString())
                .joinDate(user.getJoinDate().toString())
                .build();
    }
}
