package com.huybq.fund_management.domain.user.dto;

import lombok.Builder;

@Builder
public record UserDto (
        Long id,
        String fullName,
        String email,
        String password,
        String role,
        String phoneNumber,
        String position,
        String team,
        String dob
){
}
