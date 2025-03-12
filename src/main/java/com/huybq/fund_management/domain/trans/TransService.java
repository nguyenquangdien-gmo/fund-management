package com.huybq.fund_management.domain.trans;

import com.huybq.fund_management.domain.period.PeriodRepository;
import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransService {
    private final TransRepository transRepository;
    private final UserRepository userRepository;
    private final PeriodRepository periodRepository;

    public TransDTO createTransaction(TransDTO transDTO) {
        var user = userRepository.findById(transDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        var period = periodRepository.findById(transDTO.getPeriodId())
                .orElseThrow(() -> new ResourceNotFoundException("Period not found"));

        Trans transaction = Trans.builder()
                .amount(transDTO.getAmount())
                .transactionType(Trans.TransactionType.valueOf(transDTO.getTransactionType()))
                .description(transDTO.getDescription())
                .createdBy(user)
                .period(period)
                .build();

        transRepository.save(transaction);
        return mapToResponseDTO(transaction);
    }

    public List<TransDTO> getAllTransactions() {
        List<Trans> transactions = transRepository.findAll();
        return transactions.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private TransDTO mapToResponseDTO(Trans transaction) {
        return TransDTO.builder()
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType().name())
                .description(transaction.getDescription())
                .userId(transaction.getCreatedBy().getId())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
