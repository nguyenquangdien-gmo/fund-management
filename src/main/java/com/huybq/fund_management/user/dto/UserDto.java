package com.huybq.fund_management.user.dto;

import lombok.Builder;

@Builder
public record UserDto (
        String fullName,
        String email,
        String password,
        String role
){
}
