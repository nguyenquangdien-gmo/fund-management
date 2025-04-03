package com.huybq.fund_management.domain.invoice;

import com.huybq.fund_management.domain.fund.FundType;
import org.springframework.stereotype.Service;

@Service
public class InvoiceMapper {

    public InvoiceResponseDTO toDTO(Invoice invoice) {
        return InvoiceResponseDTO.builder()
                .id(invoice.getId())
                .name(invoice.getName())
                .invoiceType(String.valueOf(invoice.getInvoiceType()))
                .fundType(String.valueOf(invoice.getFundType()))
                .description(invoice.getDescription())
                .userId(invoice.getUser().getId())
                .amount(invoice.getAmount())
                .status(invoice.getStatus().name())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
    public Invoice toEntity(InvoiceDTO dto) {
        return Invoice.builder()
                .name(dto.name())
//                .fundType(FundType.valueOf(dto.fundType()))
                .invoiceType(InvoiceType.valueOf(dto.invoiceType()))
                .description(dto.description())
                .amount(dto.amount())
                .status(InvoiceStatus.PENDING)
                .build();
    }

}
