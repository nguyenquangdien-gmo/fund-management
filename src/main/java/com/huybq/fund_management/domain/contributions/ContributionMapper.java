package com.huybq.fund_management.domain.contributions;

import org.springframework.stereotype.Service;

@Service
public class ContributionMapper {
    public ContributionResponseDTO mapToResponseDTO(Contribution contribution) {
        ContributionResponseDTO dto = new ContributionResponseDTO();
        dto.setId(contribution.getId());
        dto.setMemberId(contribution.getUser().getId());
        dto.setMemberName(contribution.getUser().getFullName());
        dto.setPeriodId(contribution.getPeriod().getId());
        dto.setPeriodName(contribution.getPeriod().getMonth() + "/" + contribution.getPeriod().getYear());
        dto.setTotalAmount(contribution.getTotalAmount());
        dto.setPaymentStatus(contribution.getPaymentStatus());
        dto.setNote(contribution.getNote());
        dto.setDeadline(contribution.getPeriod().getDeadline());
        dto.setCreatedAt(contribution.getCreatedAt());
        dto.setIsLate(contribution.getCreatedAt() != null &&
                contribution.getCreatedAt().isAfter(contribution.getPeriod().getDeadline().atStartOfDay()));

        return dto;
    }
}
