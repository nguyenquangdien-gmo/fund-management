package com.huybq.fund_management.domain.period;

import com.huybq.fund_management.domain.fund.Fund;
import com.huybq.fund_management.domain.fund.FundRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PeriodService {
    private final PeriodRepository periodRepository;
    private final FundRepository fundRepository;

    public List<Period> getAllPeriods() {
        return periodRepository.findAll();
    }

    public Period getPeriodById(Long id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Period not found with ID: " + id));
    }

    public Period createPeriod(Period period) {
        // Lấy toàn bộ funds từ database
        List<Fund> allFunds = fundRepository.findAll();
        period.setFunds(allFunds);
        period.setMonth(LocalDate.now().getMonthValue());
        period.setYear(LocalDate.now().getYear());
        period.setDeadline(LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonthValue(), 10));

        // Tính tổng số tiền từ các funds
//        BigDecimal totalAmount = allFunds.stream()
//                .map(Fund::getAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Gán tổng số tiền vào period
//        period.setTotalAmount(totalAmount);

        // Lưu period vào database
        return periodRepository.save(period);
    }

    public Period updatePeriod(Long id, Period updatedPeriod) {
        return periodRepository.findById(id)
                .map(existingPeriod -> {
                    existingPeriod.setMonth(updatedPeriod.getMonth());
                    existingPeriod.setYear(updatedPeriod.getYear());
                    existingPeriod.setDeadline(updatedPeriod.getDeadline());
                    existingPeriod.setTotalAmount(updatedPeriod.getTotalAmount());
                    return periodRepository.save(existingPeriod);
                })
                .orElseThrow(() -> new EntityNotFoundException("Period not found with ID: " + id));
    }

    public void deletePeriod(Long id) {
        if (!periodRepository.existsById(id)) {
            throw new EntityNotFoundException("Period not found with ID: " + id);
        }
        periodRepository.deleteById(id);
    }
}
