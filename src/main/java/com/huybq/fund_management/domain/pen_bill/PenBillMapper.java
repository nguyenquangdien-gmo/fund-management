package com.huybq.fund_management.domain.pen_bill;

import com.huybq.fund_management.domain.penalty.PenaltyMapper;
import com.huybq.fund_management.domain.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PenBillMapper {
    private final UserMapper userMapper;
    private final PenaltyMapper penaltyMapper;
    public PenBillDTO toDTO(PenBill penBill) {
        return PenBillDTO.builder()
                .id(penBill.getId())
                .userId(penBill.getUser().getId())
                .penaltyId(penBill.getPenalty().getId())
                .dueDate(penBill.getDueDate())
                .description(penBill.getDescription())
                .amount(penBill.getTotalAmount())
                .paymentStatus(penBill.getPaymentStatus())
                .build();
    }
    public PenBillResponse toPenBillResponse(PenBill penBill) {
        return PenBillResponse.builder()
                .id(penBill.getId())
                .userDto(userMapper.toDto(penBill.getUser()))
                .penalty(penaltyMapper.toDTO(penBill.getPenalty()))
                .dueDate(penBill.getDueDate())
                .description(penBill.getDescription())
                .amount(penBill.getTotalAmount())
                .paymentStatus(penBill.getPaymentStatus())
                .build();
    }
}
