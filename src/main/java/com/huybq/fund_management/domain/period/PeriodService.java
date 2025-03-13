package com.huybq.fund_management.domain.period;

import com.huybq.fund_management.domain.fund.Fund;
import com.huybq.fund_management.domain.fund.FundRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PeriodService {
    private final PeriodRepository periodRepository;
    private final FundRepository fundRepository;
    private final PeriodMapper periodMapper;

    public List<PeriodDTO> getAllPeriods() {
        return periodRepository.findAll().stream()
                .map(periodMapper::toDTO)
                .toList();
    }

    public PeriodDTO getPeriodById(Long id) {
        Period period = periodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Period not found with ID: " + id));
        return periodMapper.toDTO(period);
    }

    public PeriodDTO createPeriod(PeriodDTO periodDTO) {
        LocalDate now = LocalDate.now();

        Period period = Period.builder()
                .month(periodDTO.month() != null ? periodDTO.month() : now.getMonthValue())
                .year(periodDTO.year() != null ? periodDTO.year() : now.getYear())
                .deadline(periodDTO.deadline() != null ? periodDTO.deadline() : LocalDate.of(now.getYear(), now.getMonthValue(), 10))
                .description(periodDTO.description() != null ? periodDTO.description() : "Default Description")
                .totalAmount(calculateTotalAmount())
                .build();

        period = periodRepository.save(period);
        return periodMapper.toDTO(period);
    }

    public PeriodDTO updatePeriod(Long id, PeriodDTO updatedPeriodDTO) {
        return periodRepository.findById(id)
                .map(existingPeriod -> {
                    LocalDate now = LocalDate.now();

                    existingPeriod.setMonth(updatedPeriodDTO.month() != null ? updatedPeriodDTO.month() : now.getMonthValue());
                    existingPeriod.setYear(updatedPeriodDTO.year() != null ? updatedPeriodDTO.year() : now.getYear());
                    existingPeriod.setDeadline(updatedPeriodDTO.deadline() != null ? updatedPeriodDTO.deadline() : LocalDate.of(now.getYear(), now.getMonthValue(), 10));
                    existingPeriod.setDescription(updatedPeriodDTO.description() != null ? updatedPeriodDTO.description() : existingPeriod.getDescription());
                    existingPeriod.setTotalAmount(calculateTotalAmount());

                    return periodMapper.toDTO(periodRepository.save(existingPeriod));
                })
                .orElseThrow(() -> new EntityNotFoundException("Period not found with ID: " + id));
    }

    public void deletePeriod(Long id) {
        if (!periodRepository.existsById(id)) {
            throw new EntityNotFoundException("Period not found with ID: " + id);
        }
        periodRepository.deleteById(id);
    }

    public BigDecimal calculateTotalAmount() {
        return fundRepository.findAll().stream()
                .map(Fund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
