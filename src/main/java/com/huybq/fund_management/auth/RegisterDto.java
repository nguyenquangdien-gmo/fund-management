package com.huybq.fund_management.auth;

public record RegisterDto (
        Long id,
        String fullName,
        String email,
        String password,
        String role,
        String phoneNumber,
        String position,
        String slugTeam,
        String dob
){
}
