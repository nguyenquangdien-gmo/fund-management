package com.huybq.fund_management.domain.contributions;

import com.huybq.fund_management.domain.balance.BalanceService;
import com.huybq.fund_management.domain.pen_bill.PenBillDTO;
import com.huybq.fund_management.domain.pen_bill.PenBillRepository;
import com.huybq.fund_management.domain.pen_bill.PenBillService;
import com.huybq.fund_management.domain.penalty.PenaltyDTO;
import com.huybq.fund_management.domain.penalty.PenaltyRepository;
import com.huybq.fund_management.domain.penalty.PenaltyService;
import com.huybq.fund_management.domain.period.Period;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContributionService {
    private final ContributionRepository contributionRepository;
    private final UserRepository userRepository;
    private final PeriodRepository periodRepository;
    private final TransService transService;
    private final ContributionMapper mapper;
    private final BalanceService balanceService;
    private final PenBillService penBillService;
    private final PenaltyService penaltyService;
    private final PeriodService periodService;

    public List<ContributionResponseDTO> getAllContributions() {
        List<Contribution> contributions = contributionRepository.findAll();
        return contributions.stream()
                .map(mapper::mapToResponseDTO)
                .collect(Collectors.toList());
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

    public List<UserDto> getUsersNotContributedOrOwed(int month, int year) {
        return userRepository.findUsersNotFullyContributed(month, year).stream()
                .map(user -> UserDto.builder()
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

        BigDecimal totalPeriodAmount = periodService.calculateTotalAmount();
        BigDecimal commonFundAmount = BigDecimal.valueOf(30000);

        var existingContribution = contributionRepository.findByUserIdAndPeriodId(contributionDTO.getUserId(), contributionDTO.getPeriodId());

        if (existingContribution.isPresent()) {
            throw new IllegalArgumentException("Contribution already exists for this period. Use updateContribution instead.");
        }

        BigDecimal contributedAmount = contributionDTO.getTotalAmount();
        BigDecimal contributedCommonFund = contributedAmount.min(commonFundAmount);
        BigDecimal contributedSnackFund = contributedAmount.subtract(contributedCommonFund);

        return createNewContribution(user, period, contributionDTO, totalPeriodAmount, contributedCommonFund, contributedSnackFund);
    }

    private ContributionResponseDTO createNewContribution(User user, Period period, ContributionDTO contributionDTO, BigDecimal totalPeriodAmount, BigDecimal commonFundAmount, BigDecimal snackFundAmount) {
        BigDecimal contributedAmount = contributionDTO.getTotalAmount();
        BigDecimal contributedCommonFund = contributedAmount.min(commonFundAmount);
        BigDecimal contributedSnackFund = contributedAmount.subtract(contributedCommonFund);

        BigDecimal overpaidAmount = BigDecimal.ZERO;
        BigDecimal owedAmount = totalPeriodAmount.subtract(contributedAmount);

        if (owedAmount.compareTo(BigDecimal.ZERO) < 0) {
            overpaidAmount = owedAmount.abs();
            owedAmount = BigDecimal.ZERO;
        }

        boolean isLate = LocalDateTime.now().isAfter(period.getDeadline().atStartOfDay());

        Contribution newContribution = Contribution.builder()
                .user(user)
                .period(period)
                .totalAmount(contributedAmount)
                .owedAmount(owedAmount)
                .overpaidAmount(overpaidAmount)
                .note(contributionDTO.getNote())
                .isLate(isLate)
                .build();

        if (contributedCommonFund.compareTo(BigDecimal.ZERO) > 0) {
            transService.createTransaction(TransDTO.builder()
                    .userId(user.getId())
                    .periodId(period.getId())
                    .amount(contributedCommonFund)
                    .transactionType(Trans.TransactionType.INCOME_FUND)
                    .description("Common Fund Contribution")
                    .build());
            balanceService.depositBalance("common_fund", contributedCommonFund);
        }

        if (contributedSnackFund.compareTo(BigDecimal.ZERO) > 0) {
            transService.createTransaction(TransDTO.builder()
                    .userId(user.getId())
                    .periodId(period.getId())
                    .amount(contributedSnackFund)
                    .transactionType(Trans.TransactionType.INCOME_FUND)
                    .description("Snack Fund Contribution")
                    .build());
            balanceService.depositBalance("snack_fund", contributedSnackFund);
        }

        if (overpaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            transService.createTransaction(TransDTO.builder()
                    .userId(user.getId())
                    .periodId(period.getId())
                    .amount(overpaidAmount)
                    .transactionType(Trans.TransactionType.INCOME_FUND)
                    .description("Overpaid contribution amount")
                    .build());
        }

        if (contributedAmount.compareTo(totalPeriodAmount) >= 0) {
            newContribution.setPaymentStatus(Contribution.PaymentStatus.PAID);
        } else {
            newContribution.setPaymentStatus(Contribution.PaymentStatus.PARTIAL);
        }

        contributionRepository.save(newContribution);

        if (isLate) {
            createLatePenalty(user);
        }

        return mapper.mapToResponseDTO(newContribution);
    }

    @Transactional
    public ContributionResponseDTO updateContribution(Long contributionId, ContributionDTO contributionDTO) {
        var contribution = contributionRepository.findById(contributionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));

        BigDecimal totalPeriodAmount = BigDecimal.valueOf(150000);
        BigDecimal commonFundAmount = BigDecimal.valueOf(30000);
        BigDecimal contributedAmount = contributionDTO.getTotalAmount();
        BigDecimal alreadyPaid = contribution.getTotalAmount();
        BigDecimal remainingAmount = totalPeriodAmount.subtract(alreadyPaid);

        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("You have already fully contributed for this period");
        }

        BigDecimal overpaidAmount = BigDecimal.ZERO;

        if (contributedAmount.compareTo(remainingAmount) > 0) {
            overpaidAmount = contributedAmount.subtract(remainingAmount);
            contributedAmount = remainingAmount;
        }

        BigDecimal alreadyPaidToCommonFund = alreadyPaid.min(commonFundAmount);
        boolean isCommonFundFullyPaid = alreadyPaidToCommonFund.compareTo(commonFundAmount) >= 0;

        BigDecimal contributedCommonFund = BigDecimal.ZERO;
        BigDecimal contributedSnackFund = contributedAmount;

        if (!isCommonFundFullyPaid) {
            BigDecimal remainingCommonFund = commonFundAmount.subtract(alreadyPaidToCommonFund);
            contributedCommonFund = contributedAmount.min(remainingCommonFund);
            contributedSnackFund = contributedAmount.subtract(contributedCommonFund);
        }

        contribution.setTotalAmount(alreadyPaid.add(contributedAmount));
        contribution.setOwedAmount(totalPeriodAmount.subtract(contribution.getTotalAmount()));
        contribution.setOverpaidAmount(overpaidAmount);
        contribution.getUser().setTotalOverpaidAmount(overpaidAmount);


        if (contributedCommonFund.compareTo(BigDecimal.ZERO) > 0) {
            transService.createTransaction(TransDTO.builder()
                    .userId(contribution.getUser().getId())
                    .periodId(contribution.getPeriod().getId())
                    .amount(contributedCommonFund)
                    .transactionType(Trans.TransactionType.INCOME_FUND)
                    .description("Common Fund Contribution")
                    .build());
            balanceService.depositBalance("common_fund", contributedCommonFund);
        }

        if (contributedSnackFund.compareTo(BigDecimal.ZERO) > 0) {
            transService.createTransaction(TransDTO.builder()
                    .userId(contribution.getUser().getId())
                    .periodId(contribution.getPeriod().getId())
                    .amount(contributedSnackFund)
                    .transactionType(Trans.TransactionType.INCOME_FUND)
                    .description("Snack Fund Contribution")
                    .build());
            balanceService.depositBalance("snack_fund", contributedSnackFund);
        }

        if (overpaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            contribution.setOverpaidAmount(contribution.getOverpaidAmount().add(overpaidAmount));
            transService.createTransaction(TransDTO.builder()
                    .userId(contribution.getUser().getId())
                    .periodId(contribution.getPeriod().getId())
                    .amount(overpaidAmount)
                    .transactionType(Trans.TransactionType.INCOME_FUND)
                    .description("Overpaid Contribution")
                    .build());
        }

        if (contribution.getTotalAmount().compareTo(totalPeriodAmount) >= 0) {
            contribution.setPaymentStatus(Contribution.PaymentStatus.PAID);
        } else {
            contribution.setPaymentStatus(Contribution.PaymentStatus.PARTIAL);
        }

        boolean isLate = LocalDateTime.now().isAfter(contribution.getPeriod().getDeadline().atStartOfDay());
        contribution.setIsLate(isLate);

        contributionRepository.save(contribution);
        return mapper.mapToResponseDTO(contribution);
    }


    private void createLatePenalty(User user) {
        PenaltyDTO penalty = penaltyService.getPenaltyByName("contribute_late");

        // Tạo PenBill thông qua service
        PenBillDTO penBillDTO = PenBillDTO.builder()
                .userId(user.getId())
                .penaltyId(penalty.getId())
                .amount(penalty.getAmount())
                .dueDate(LocalDate.now())
                .description(penalty.getDescription())
                .build();

        penBillService.createPenBill(penBillDTO);

    }

    public List<ContributionResponseDTO> getOwedContributionsByUser(Long userId) {
        return contributionRepository.findOwedContributionsByUserId(userId).stream().map(mapper::mapToResponseDTO).collect(Collectors.toList());
    }

}
