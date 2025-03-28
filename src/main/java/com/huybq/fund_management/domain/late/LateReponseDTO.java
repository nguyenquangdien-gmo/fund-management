package com.huybq.fund_management.domain.late;

import com.huybq.fund_management.domain.user.entity.User;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class LateReponseDTO {
    private Long id;
    private User user;
    private LocalDate date;
    private LocalTime checkinAt;
    private String note;
}
