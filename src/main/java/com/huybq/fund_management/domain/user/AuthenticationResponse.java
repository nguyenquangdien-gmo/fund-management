package com.huybq.fund_management.domain.user;

import lombok.Builder;

@Builder
public record AuthenticationResponse(
        String accessToken,
        String refreshToken,
        UserDto user,
        String password
) {
}
