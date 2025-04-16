package com.huybq.fund_management.domain.late;

import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LateDTO {
    private UserResponseDTO user;
    private int lateCount;
}
