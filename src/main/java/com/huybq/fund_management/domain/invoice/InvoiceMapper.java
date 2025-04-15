package com.huybq.fund_management.domain.invoice;

import com.huybq.fund_management.domain.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceMapper {
    private final UserMapper mapper;

    public InvoiceResponseDTO toDTO(Invoice invoice) {
        return InvoiceResponseDTO.builder()
                .id(invoice.getId())
                .name(invoice.getName())
                .invoiceType(String.valueOf(invoice.getInvoiceType()))
                .fundType(String.valueOf(invoice.getFundType()))
                .description(invoice.getDescription())
                .user(mapper.toDto(invoice.getUser()))
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
