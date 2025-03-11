package com.huybq.fund_management.auth;

public record AuthenticationDto(
        String email,
        String password
) {
}
