package com.huybq.fund_management.domain.trans;

import com.huybq.fund_management.domain.period.Period;
import com.huybq.fund_management.domain.period.PeriodRepository;
import com.huybq.fund_management.domain.user.dto.UserDto;
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

    public void createTransaction(TransDTO transDTO) {
        var user = userRepository.findById(transDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Period period = null;
        if (transDTO.getPeriodId() != null) {
            period = periodRepository.findById(transDTO.getPeriodId())
                    .orElseThrow(() -> new ResourceNotFoundException("Period not found"));
        }

        Trans transaction = Trans.builder()
                .amount(transDTO.getAmount())
                .description(transDTO.getDescription())
                .transactionType(transDTO.getTransactionType())
                .createdBy(user)
                .period(period)
                .build();

        transRepository.save(transaction);
        mapToResponseDTO(transaction);
    }

    public List<TransReponseDTO> getAllTransactions() {
        List<Trans> transactions = transRepository.findAllByOrderByCreatedAtDesc();
        return transactions.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private TransReponseDTO mapToResponseDTO(Trans transaction) {

        var user = transaction.getCreatedBy();
        var userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().getName())
                .build();
        return TransReponseDTO.builder()
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .description(transaction.getDescription())
                .userDto(userDto)
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
