package com.huybq.fund_management.domain.contributions;

import com.huybq.fund_management.domain.balance.BalanceService;
import com.huybq.fund_management.domain.fund.FundType;
import com.huybq.fund_management.domain.period.PeriodRepository;
import com.huybq.fund_management.domain.trans.Trans;
import com.huybq.fund_management.domain.trans.TransDTO;
import com.huybq.fund_management.domain.trans.TransService;
import com.huybq.fund_management.domain.user.UserDto;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
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
        return contributionRepository.findAllOrderByPaymentStatusPriority()
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
                        .role(user.getRole().getName())
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
        List<Contribution> contributions = contributionRepository.findByUserIdAndPaymentStatusOrderByCreatedAtDesc(
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


    public ContributionStatsDTO getYearContributionStats(int year) {
        return contributionRepository.getYearContributionStatistics(year);
    }

    public List<UserDto> getUsersOwedContributed(int month, int year) {
        return userRepository.findUsersOwedContributed(month, year).stream()
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole().getName())
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

        BigDecimal actualAmount = contributionDTO.getTotalAmount(); // Số tiền user muốn đóng
        boolean isLate = LocalDateTime.now().isAfter(period.getDeadline().atStartOfDay());

        Contribution newContribution = Contribution.builder()
                .user(user)
                .period(period)
                .totalAmount(actualAmount)
                .note(period.getDescription())
                .paymentStatus(Contribution.PaymentStatus.PENDING)
                .fundType(!Objects.equals(contributionDTO.getFundType(), "") ? FundType.valueOf(contributionDTO.getFundType()) : null)
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

        if (contribution.getFundType() != null) {
            FundType fundType = contribution.getFundType();
            String fundKey = fundType.name().toLowerCase(); // "common" hoặc "snack"

            transService.createTransaction(TransDTO.builder()
                    .userId(contribution.getUser().getId())
                    .periodId(contribution.getPeriod().getId())
                    .amount(totalAmount)
                    .transactionType(Trans.TransactionType.INCOME_FUND)
                    .description("Đóng quỹ " + fundKey)
                    .build());

            balanceService.depositBalance(fundKey, totalAmount);
        } else {
            // Không có fundType => chia theo mặc định
            BigDecimal commonFundAmount = totalAmount.min(BigDecimal.valueOf(30000));
            BigDecimal snackFundAmount = totalAmount.subtract(commonFundAmount);

            // Cộng quỹ chung
            transService.createTransaction(TransDTO.builder()
                    .userId(contribution.getUser().getId())
                    .periodId(contribution.getPeriod().getId())
                    .amount(commonFundAmount)
                    .transactionType(Trans.TransactionType.INCOME_FUND)
                    .description("Đóng quỹ chung")
                    .build());
            balanceService.depositBalance("common", commonFundAmount);

            // Cộng quỹ ăn vặt
            transService.createTransaction(TransDTO.builder()
                    .userId(contribution.getUser().getId())
                    .periodId(contribution.getPeriod().getId())
                    .amount(snackFundAmount)
                    .transactionType(Trans.TransactionType.INCOME_FUND)
                    .description("Đóng quỹ ăn vặt")
                    .build());
            balanceService.depositBalance("snack", snackFundAmount);
        }

        contribution.setPaymentStatus(Contribution.PaymentStatus.PAID);
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
    public void rejectContribution(Long contributionId, String reason) {
        var contribution = contributionRepository.findById(contributionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));

        if (contribution.getPaymentStatus() == Contribution.PaymentStatus.PENDING) {
            contribution.setPaymentStatus(Contribution.PaymentStatus.CANCELED);
            if (reason != null) {
                contribution.setNote(contribution.getNote()+": Bị hủy vì "+reason);
            }
            contributionRepository.save(contribution);
            return;
        }
        throw new IllegalArgumentException("Invalid state for rejection or cancellation");
    }


    public BigDecimal getTotalContributionAmountByPeriod(int year) {
        return contributionRepository.getTotalPaidContributionsByYear(year);
    }


}
