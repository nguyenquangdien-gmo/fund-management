package com.huybq.fund_management.domain.late;

import com.huybq.fund_management.domain.pen_bill.PenBillDTO;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserResponseDTO;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LateWithPenBillDTO {

    private Long id;
    private UserResponseDTO user;
    private LocalDate date;
    private LocalTime checkinAt;
    private String note;

    private PenBillDTO penBill;
}

