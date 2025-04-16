package com.huybq.fund_management.domain.user;

import lombok.Builder;
import lombok.Data;

@Builder
public record UserResponseDTO(
        Long id,
        String fullName,
        String email,
        String role,
        String phoneNumber,
        String position,
        String team,
        String dob,
        String joinDate
) {

}
