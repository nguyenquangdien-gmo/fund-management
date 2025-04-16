package com.huybq.fund_management.domain.user;

import lombok.Builder;

@Builder
public record UserDTO(
        Long id,
        String fullName,
        String email,
        String password,
        String role,
        String phoneNumber,
        String position,
        String slugTeam,
        String dob,
        String joinDate,
        String userIdChat
){
}
