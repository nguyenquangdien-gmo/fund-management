package com.huybq.fund_management.auth;

public record ChangePasswordDto(
        String email,
        String oldPassword,
        String newPassword
) {
}
