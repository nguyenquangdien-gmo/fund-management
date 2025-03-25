package com.huybq.fund_management.domain.late;

import com.huybq.fund_management.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Late {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate date;
    private LocalTime checkinAt;
    private String note;

}
