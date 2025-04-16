package com.huybq.fund_management.domain.late;

import com.huybq.fund_management.domain.user.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LateResponseDTO {
    private Long id;
    private UserResponseDTO user;
    private LocalDate date;
    private LocalTime checkinAt;
    private String note;
}
