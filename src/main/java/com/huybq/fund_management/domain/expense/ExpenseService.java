package com.huybq.fund_management.domain.expense;

import com.huybq.fund_management.domain.balance.BalanceService;
import com.huybq.fund_management.domain.period.Period;
import com.huybq.fund_management.domain.trans.Trans;
import com.huybq.fund_management.domain.trans.TransDTO;
import com.huybq.fund_management.domain.trans.TransService;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository repository;
    private final ExpenseMapper mapper;
    private final UserRepository userRepository;
    private final BalanceService balanceService;
    private final TransService transService;

    public List<ExpenseDTO> getExpenses() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    public BigDecimal getTotalAmount() {
        return repository.findAll().stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<ExpenseDTO> getExpensesByMonthAndYear(int month, int year) {
        return repository.findByMonthAndYear(month, year).stream()
                .map(mapper::toDTO)
                .toList();
    }

    public BigDecimal getTotalAmountByMonthAndYear(int month, int year) {
        return repository.getTotalExpenseByMonthAndYear(month, year);
    }

    public BigDecimal getTotalAmountByYear(int year) {
        return repository.getTotalExpenseByYear(year);
    }

    public ExpenseDTO create(ExpenseDTO dto) {
        var user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + dto.userId()));
        var balance = balanceService.findBalanceByTitle("common_fund");
        if (balance.getTotalAmount().compareTo(dto.amount()) < 0) {
            throw new IllegalStateException("Not enough balance in the common fund to cover this expense");
        }

        balanceService.withdrawBalance("common_fund", dto.amount());

        var expense = mapper.toEntity(dto);
        expense.setUser(user);
        expense = repository.save(expense);

        transService.createTransaction(TransDTO.builder()
                .userId(expense.getUser().getId())
                .amount(expense.getAmount())
                .transactionType(Trans.TransactionType.EXPENSE)
                .description("Expense recorded: " + dto.name())
                .build());

        return mapper.toDTO(expense);
    }

    public ExpenseDTO update(Long idExpense, ExpenseDTO dto) {
        return repository.findById(idExpense)
                .map(expense -> {
                    var user = userRepository.findById(dto.userId()).orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + dto.userId()));
                    expense.setUser(user);
                    expense.setName(dto.name());
                    expense.setAmount(dto.amount());
                    expense.setDescription(dto.description());
                    TransDTO transDTO = TransDTO.builder()
                            .amount(dto.amount())
                            .description("Expense recorded: " + dto.name())
                            .transactionType(Trans.TransactionType.EXPENSE)
                            .userId(user.getId())
                            .build();
                    transService.createTransaction(transDTO);
                    return mapper.toDTO(repository.save(expense));
                })
                .orElseThrow(() -> new EntityNotFoundException("Expense not found with ID: " + idExpense));

    }

    public void delete(Long idExpense) {
        repository.deleteById(idExpense);
    }
}
