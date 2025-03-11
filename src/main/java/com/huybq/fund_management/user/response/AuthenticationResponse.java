package com.huybq.fund_management.user.response;

import com.huybq.fund_management.user.dto.UserDto;
import lombok.Builder;

@Builder
public record AuthenticationResponse(

        String accessToken,
        String refreshToken,
        UserDto user
) {
}
