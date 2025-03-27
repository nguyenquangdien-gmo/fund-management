package com.huybq.fund_management.domain.contributions;

import com.huybq.fund_management.domain.balance.BalanceService;
import com.huybq.fund_management.domain.period.PeriodRepository;
import com.huybq.fund_management.domain.period.PeriodService;
import com.huybq.fund_management.domain.trans.Trans;
import com.huybq.fund_management.domain.trans.TransDTO;
import com.huybq.fund_management.domain.trans.TransService;
import com.huybq.fund_management.domain.user.dto.UserDto;
import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContributionService {
    private final ContributionRepository contributionRepository;
    private final UserRepository userRepository;
    private final PeriodRepository periodRepository;
    private final TransService transService;
    private final ContributionMapper mapper;
    private final BalanceService balanceService;

    public List<ContributionResponseDTO> getAllContributions() {
        List<Contribution> contributions = contributionRepository.findAll();
        return contributions.stream()
                .map(mapper::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ContributionResponseDTO> getPendingContributions() {
        return contributionRepository.findByPaymentStatusIn(List.of(
                        Contribution.PaymentStatus.PENDING

                ))
                .stream()
                .map(mapper::mapToResponseDTO)
                .toList();
    }
    public Contribution findById(Long id) {
        return contributionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));
    }

    public List<ContributionResponseDTO> findAllContributions(Long periodId) {
        List<Contribution> contributions = contributionRepository.findAllByPeriodId(periodId);
        return contributions.stream().map(mapper::mapToResponseDTO).toList();
    }

    public List<UserDto> getUsersContributedInPeriod(Long periodId) {
        return contributionRepository.findUsersByPeriodId(periodId).stream()
                .map(user -> UserDto.builder()
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole().name())
                        .build()
                )
                .toList();
    }


    public List<ContributionResponseDTO> getAllContributionsByMonthAndYear(int month, int year) {
        List<Contribution> contributions = contributionRepository.findAllByPeriod_MonthAndPeriod_Year(month, year);
        return contributions.stream().map(mapper::mapToResponseDTO).toList();
    }

    public List<ContributionResponseDTO> getAllContributionsByMember(Long userId) {
        List<Contribution> contributions = contributionRepository.findByUserId(userId);
        return contributions.stream()
                .map(mapper::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ContributionResponseDTO> getPendingContributionsByMember(Long userId) {
        List<Contribution> contributions = contributionRepository.findByUserIdAndPaymentStatus(
                userId, Contribution.PaymentStatus.PENDING);
        return contributions.stream()
                .map(mapper::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getMonthlyContributionStats(int year) {
        List<Object[]> results = contributionRepository.getMonthlyContributionStatistics(year);
        List<Map<String, Object>> response = new ArrayList<>();

        for (Object[] row : results) {
            if (row.length >= 2) {  // Kiểm tra độ dài tránh lỗi Index Out of Bounds
                Map<String, Object> data = new HashMap<>();
                data.put("month", row[0]);  // Tháng
                data.put("totalAmount", row[1]);  // Tổng tiền đã đóng

                response.add(data);
            }
        }
        return response;
    }


    public List<Map<String, Object>> getYearlyContributionStats() {
        List<Object[]> results = contributionRepository.getYearlyContributionStatistics();
        List<Map<String, Object>> stats = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("year", row[0]);
            stat.put("totalAmount", row[1]);
            stats.add(stat);
        }
        return stats;
    }

    public List<UserDto> getUsersOwedContributed(int month, int year) {
        return userRepository.findUsersOwedContributed(month, year).stream()
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole().name())
                        .build()
                ).toList();
    }

    public List<Map<String, Object>> getLateContributors() {
        List<Object[]> results = contributionRepository.getLateContributors();
        List<Map<String, Object>> lateContributors = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> lateUser = new HashMap<>();
            lateUser.put("user", row[0]);
            lateUser.put("paidAt", row[1]);
            lateUser.put("amount", row[2]);
            lateContributors.add(lateUser);
        }
        return lateContributors;
    }


    @Transactional
    public ContributionResponseDTO createContribution(ContributionDTO contributionDTO) {
        var user = userRepository.findById(contributionDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var period = periodRepository.findById(contributionDTO.getPeriodId())
                .orElseThrow(() -> new ResourceNotFoundException("Period not found"));
        var existingContributions = contributionRepository.findByUserIdAndPeriodId(contributionDTO.getUserId(), contributionDTO.getPeriodId());

        // Kiểm tra xem có contribution nào không bị CANCELED không
        boolean hasValidContribution = existingContributions.stream()
                .anyMatch(c -> c.getPaymentStatus() != Contribution.PaymentStatus.CANCELED);

        if (hasValidContribution) {
            throw new IllegalArgumentException("Contribution already exists for this period. Use updateContribution instead.");
        }


        BigDecimal needAmount = period.getTotalAmount();
        BigDecimal actualAmount = contributionDTO.getTotalAmount(); // Số tiền user muốn đóng

        if (actualAmount.compareTo(needAmount) < 0) {
            throw new IllegalArgumentException("The amount is not enough to cover the total amount of the period.");
        }

        boolean isLate = LocalDateTime.now().isAfter(period.getDeadline().atStartOfDay());

        Contribution newContribution = Contribution.builder()
                .user(user)
                .period(period)
                .totalAmount(actualAmount)
                .note(contributionDTO.getNote())
                .paymentStatus(Contribution.PaymentStatus.PENDING)
                .isLate(isLate)
                .build();

        contributionRepository.save(newContribution);
        return mapper.mapToResponseDTO(newContribution);
    }

    @Transactional
    public ContributionResponseDTO updateContribution(Long contributionId) {
        var contribution = contributionRepository.findById(contributionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));

        if (contribution.getPaymentStatus() == Contribution.PaymentStatus.PAID) {
            throw new IllegalArgumentException("Cannot update a fully paid contribution");
        }

        contribution.setPaymentStatus(Contribution.PaymentStatus.PENDING);
        contributionRepository.save(contribution);
        return mapper.mapToResponseDTO(contribution);
    }


    @Transactional
    public void approveContribution(Long contributionId) {
        Contribution contribution = contributionRepository.findById(contributionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));
        if (contribution.getUser() == null) {
            throw new ResourceNotFoundException("User not found for contribution: " + contributionId);
        }
        if (contribution.getPeriod() == null) {
            throw new ResourceNotFoundException("Period not found for contribution: " + contributionId);
        }
        if (contribution.getTotalAmount() == null) {
            throw new IllegalArgumentException("Total amount is null for contribution: " + contributionId);
        }
        if (contribution.getPaymentStatus() == Contribution.PaymentStatus.PAID) {
            throw new IllegalArgumentException("Contribution is already paid");
        }

        BigDecimal totalAmount = contribution.getTotalAmount();
        BigDecimal needAmount = contribution.getPeriod().getTotalAmount();
        BigDecimal commonFundAmount = totalAmount.min(BigDecimal.valueOf(30000));
        BigDecimal snackFundAmount = totalAmount.subtract(commonFundAmount);

        // Cập nhật balance cho Common Fund
        transService.createTransaction(TransDTO.builder()
                .userId(contribution.getUser().getId())
                .periodId(contribution.getPeriod().getId())
                .amount(commonFundAmount)
                .transactionType(Trans.TransactionType.INCOME_FUND)
                .description("Common Fund Contribution")
                .build());
        balanceService.depositBalance("common_fund", commonFundAmount);

        // Cập nhật balance cho Snack Fund
        transService.createTransaction(TransDTO.builder()
                .userId(contribution.getUser().getId())
                .periodId(contribution.getPeriod().getId())
                .amount(snackFundAmount)
                .transactionType(Trans.TransactionType.INCOME_FUND)
                .description("Snack Fund Contribution")
                .build());
        balanceService.depositBalance("snack_fund", snackFundAmount);

        if (totalAmount.compareTo(needAmount) >= 0) {
            contribution.setPaymentStatus(Contribution.PaymentStatus.PAID);
        } else {
            throw new IllegalArgumentException("Contribution is not fully paid");
        }
        contributionRepository.save(contribution);
    }


//    @Transactional
//    public void rejectNewContribution(Long contributionId) {
//        var contribution = contributionRepository.findById(contributionId)
//                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));
//
//        if (contribution.getPaymentStatus() != Contribution.PaymentStatus.PENDING) {
//            throw new IllegalArgumentException("Only pending contributions can be rejected");
//        }
//
//        contributionRepository.deleteById(contributionId);
//    }

    @Transactional
    public void rejectContribution(Long contributionId) {
        var contribution = contributionRepository.findById(contributionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));

        if (contribution.getPaymentStatus() == Contribution.PaymentStatus.PENDING) {
            contribution.setPaymentStatus(Contribution.PaymentStatus.CANCELED);
            contributionRepository.save(contribution);
            return;
        }
        throw new IllegalArgumentException("Invalid state for rejection or cancellation");
    }


    public BigDecimal getTotalContributionAmountByPeriod(int year) {
        return contributionRepository.getTotalPaidContributionsByYear(year);
    }


}
