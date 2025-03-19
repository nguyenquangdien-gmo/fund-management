package com.huybq.fund_management.domain.expense;

import com.huybq.fund_management.domain.fund.FundType;
import org.springframework.stereotype.Service;

@Service
public class ExpenseMapper {

    public ExpenseDTO toDTO(Expense expense) {
        return ExpenseDTO.builder()
                .id(expense.getId())
                .name(expense.getName())
                .expenseType(String.valueOf(expense.getExpenseType()))
                .description(expense.getDescription())
                .userId(expense.getUser().getId())
                .amount(expense.getAmount())
                .createdAt(expense.getCreatedAt())
                .build();
    }
    public Expense toEntity(ExpenseDTO dto) {
        return Expense.builder()
                .name(dto.name())
                .expenseType(FundType.valueOf(dto.expenseType()))
                .description(dto.name())
                .amount(dto.amount())
                .build();
    }

}
