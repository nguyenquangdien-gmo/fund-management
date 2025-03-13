package com.huybq.fund_management.domain.pen_bill;

import org.springframework.stereotype.Service;

@Service
public class PenBillMapper {
    public PenBillDTO toDTO(PenBill penBill) {
        return PenBillDTO.builder()
                .userId(penBill.getUser().getId())
                .penaltyId(penBill.getPenalty().getId())
                .dueDate(penBill.getDueDate())
                .description(penBill.getDescription())
                .amount(penBill.getTotalAmount())
                .paymentStatus(penBill.getPaymentStatus())
                .build();
    }
}
