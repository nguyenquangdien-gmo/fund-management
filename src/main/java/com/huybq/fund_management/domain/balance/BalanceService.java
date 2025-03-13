package com.huybq.fund_management.domain.balance;

import com.huybq.fund_management.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BalanceService {
    private final BalanceRepository repository;

    public Balance findBalanceByTitle(String title) {
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        return repository.findBalanceByTitle(title).get();
    }

    public List<Balance> findAllBalances() {
        return repository.findAll();
    }

    public Balance findBalanceById(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Balance not found with id: " + id));
    }

    @Transactional
    public Balance createBalance(BalanceDTO dto) {
        var balance = Balance.builder()
                .title(dto.title())
                .totalAmount(dto.totalAmount())
                .build();
        return repository.save(balance);
    }

    @Transactional
    public Balance updateBalance(Integer balanceId, BalanceDTO dto) {
        var balance = findBalanceById(balanceId);
        balance.setTitle(dto.title());
        balance.setTotalAmount(dto.totalAmount());
        return repository.save(balance);
    }
    @Transactional
    public void depositBalance(String title, BigDecimal amount) {
        var balance = findBalanceByTitle(title.toLowerCase());
        balance.setTotalAmount(balance.getTotalAmount().add(amount));
        repository.save(balance);
    }
    @Transactional
    public Balance withdrawBalance(String title, BigDecimal amount) {
        var balance = findBalanceByTitle(title.toLowerCase());
        balance.setTotalAmount(balance.getTotalAmount().subtract(amount));
        return repository.save(balance);
    }
    @Transactional
    public void deleteBalance(Integer id) {
        repository.deleteById(id);
    }

}
