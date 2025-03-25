package com.huybq.fund_management.domain.contributions;

import com.huybq.fund_management.domain.balance.BalanceService;
import com.huybq.fund_management.domain.period.PeriodRepository;
import com.huybq.fund_management.domain.period.PeriodService;
import com.huybq.fund_management.domain.trans.Trans;
import com.huybq.fund_management.domain.trans.TransDTO;
import com.huybq.fund_management.domain.trans.TransService;
import com.huybq.fund_management.domain.user.dto.UserDto;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
                        Contribution.PaymentStatus.PENDING,
                        Contribution.PaymentStatus.UPDATE
                ))
                .stream()
                .map(mapper::mapToResponseDTO)
                .toList();
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

        var existingContribution = contributionRepository.findByUserIdAndPeriodId(contributionDTO.getUserId(), contributionDTO.getPeriodId());
        if (existingContribution.isPresent()) {
            throw new IllegalArgumentException("Contribution already exists for this period. Use updateContribution instead.");
        }

        BigDecimal needAmount = period.getTotalAmount(); // Giả sử số tiền cần đóng được lưu trong period
        boolean isLate = LocalDateTime.now().isAfter(period.getDeadline().atStartOfDay());

        Contribution newContribution = Contribution.builder()
                .user(user)
                .period(period)
                .totalAmount(contributionDTO.getTotalAmount())
                .owedAmount(contributionDTO.getTotalAmount().compareTo(needAmount) < 0 ? needAmount.subtract(contributionDTO.getTotalAmount()) : BigDecimal.ZERO)
                .overpaidAmount(contributionDTO.getTotalAmount().compareTo(needAmount) > 0 ? contributionDTO.getTotalAmount().subtract(needAmount) : BigDecimal.ZERO)
                .note(contributionDTO.getNote())
                .paymentStatus(Contribution.PaymentStatus.PENDING)
                .isLate(isLate)
                .build();

        contributionRepository.save(newContribution);
        return mapper.mapToResponseDTO(newContribution);
    }

    @Transactional
    public ContributionResponseDTO updateContribution(Long contributionId, ContributionDTO contributionDTO) {
        var contribution = contributionRepository.findById(contributionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));

        if (contribution.getPaymentStatus() == Contribution.PaymentStatus.PAID) {
            throw new IllegalArgumentException("Cannot update a fully paid contribution");
        }

        BigDecimal needAmount = contribution.getPeriod().getTotalAmount();

        // Lưu trạng thái trước khi cập nhật
        contribution.setPreviousTotalAmount(contribution.getTotalAmount());
        contribution.setPreviousOwedAmount(contribution.getOwedAmount());
        contribution.setPreviousOverpaidAmount(contribution.getOverpaidAmount());
        contribution.setPreviousStatus(contribution.getPaymentStatus());

        // Cộng thêm số tiền mới vào số tiền hiện tại
        BigDecimal newTotalAmount = contribution.getTotalAmount().add(contributionDTO.getTotalAmount());

        // Tính toán số tiền còn thiếu hoặc dư
        BigDecimal owedAmount = newTotalAmount.compareTo(needAmount) < 0 ? needAmount.subtract(newTotalAmount) : BigDecimal.ZERO;
        BigDecimal overpaidAmount = newTotalAmount.compareTo(needAmount) > 0 ? newTotalAmount.subtract(needAmount) : BigDecimal.ZERO;

        // **Luôn giữ trạng thái `PENDING` để đợi admin approve**
        Contribution.PaymentStatus newStatus = Contribution.PaymentStatus.PENDING;

        // Cập nhật Contribution
        contribution.setTotalAmount(newTotalAmount);
        contribution.setOwedAmount(owedAmount);
        contribution.setOverpaidAmount(overpaidAmount);
        contribution.setPaymentStatus(newStatus);

        contributionRepository.save(contribution);
        return mapper.mapToResponseDTO(contribution);
    }


    @Transactional
    public void approveContribution(Long contributionId) {
        Contribution contribution = contributionRepository.findById(contributionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));

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

        // Nếu số tiền đóng đủ hoặc dư -> PAID, nếu thiếu -> PARTIAL
        if (totalAmount.compareTo(needAmount) >= 0) {
            contribution.setPaymentStatus(Contribution.PaymentStatus.PAID);
            contribution.setOverpaidAmount(totalAmount.subtract(needAmount)); // Nếu dư tiền
            balanceService.depositBalance("overpaid_fund", contribution.getOverpaidAmount());
        } else {
            contribution.setPaymentStatus(Contribution.PaymentStatus.PARTIAL);
            contribution.setOwedAmount(needAmount.subtract(totalAmount)); // Nếu thiếu tiền
        }

        contributionRepository.save(contribution);
    }

    @Transactional
    public void rejectNewContribution(Long contributionId) {
        var contribution = contributionRepository.findById(contributionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));

        if (contribution.getPaymentStatus() != Contribution.PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Only pending contributions can be rejected");
        }

        contributionRepository.deleteById(contributionId);
    }

    @Transactional
    public void rejectOrCancelContribution(Long contributionId) {
        var contribution = contributionRepository.findById(contributionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));

        // Nếu contribution đang chờ duyệt (tạo mới) -> Xóa luôn
        if (contribution.getPaymentStatus() == Contribution.PaymentStatus.PENDING) {
            contributionRepository.deleteById(contributionId);
            return;
        }

        // Nếu contribution đã được cập nhật (PARTIAL), cần khôi phục dữ liệu cũ
        if (contribution.getPaymentStatus() == Contribution.PaymentStatus.PARTIAL &&
                contribution.getPreviousTotalAmount() != null && contribution.getPreviousStatus() != null) {

            contribution.setTotalAmount(contribution.getPreviousTotalAmount());
            contribution.setOwedAmount(contribution.getPreviousOwedAmount());
            contribution.setOverpaidAmount(contribution.getPreviousOverpaidAmount());
            contribution.setPaymentStatus(contribution.getPreviousStatus());

            // Xóa dữ liệu cũ sau khi khôi phục
            contribution.setPreviousTotalAmount(null);
            contribution.setPreviousOwedAmount(null);
            contribution.setPreviousOverpaidAmount(null);
            contribution.setPreviousStatus(null);

            contributionRepository.save(contribution);
            return;
        }

        throw new IllegalArgumentException("Invalid state for rejection or cancellation");
    }


    public List<ContributionResponseDTO> getOwedContributionsByUser(Long userId) {
        return contributionRepository.findOwedContributionsByUserId(userId).stream().map(mapper::mapToResponseDTO).collect(Collectors.toList());
    }

    public BigDecimal getTotalContributionAmountByPeriod(int year ) {
        return contributionRepository.getTotalPaidContributionsByYear(year);
    }


}
