package com.huybq.fund_management.domain.period;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
@Component
public class PeriodMapper {
    public  PeriodDTO toDTO(Period period) {
        if (period == null) {
            return null;
        }
        return new PeriodDTO(
                period.getId(),
                period.getMonth(),
                period.getYear(),
                period.getDeadline(),
                period.getDescription(),
                period.getTotalAmount()
        );
    }
    public  Period toEntity(PeriodDTO periodDTO) {
        if (periodDTO == null) {
            return null;
        }

        Period period = new Period();
        period.setMonth(periodDTO.month() != null ? periodDTO.month() : LocalDate.now().getMonthValue());
        period.setYear(periodDTO.year() != null ? periodDTO.year() : LocalDate.now().getYear());
        period.setDeadline(periodDTO.deadline() != null ? periodDTO.deadline() : LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonthValue(), 10));
        period.setDescription(periodDTO.description());

        return period;
    }
}
