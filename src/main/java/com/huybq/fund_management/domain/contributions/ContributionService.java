package com.huybq.fund_management.domain.contributions;

import com.huybq.fund_management.domain.balance.BalanceService;
import com.huybq.fund_management.domain.fund.FundType;
import com.huybq.fund_management.domain.pen_bill.PenBillResponse;
import com.huybq.fund_management.domain.period.PeriodRepository;
import com.huybq.fund_management.domain.trans.Trans;
import com.huybq.fund_management.domain.trans.TransDTO;
import com.huybq.fund_management.domain.trans.TransService;
import com.huybq.fund_management.domain.user.*;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import com.huybq.fund_management.utils.chatops.Notification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
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
    private final UserMapper userMapper;
    private final Notification notification;

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

    public ContributionResponseDTO findById(Long id) {
        return contributionRepository.findById(id).map(mapper::mapToResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));
    }

    public List<ContributionResponseDTO> findAllContributions(Long periodId) {
        List<Contribution> contributions = contributionRepository.findAllByPeriodId(periodId);
        return contributions.stream().map(mapper::mapToResponseDTO).toList();
    }

    public List<UserResponseDTO> getUsersContributedInPeriod(Long periodId) {
        return contributionRepository.findUsersByPeriodId(periodId).stream()
                .map(userMapper::toResponseDTO)
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

    public List<UserResponseDTO> getUsersOwedContributed(int month, int year) {
        return userRepository.findUsersOwedContributed(month, year).stream()
                .map(userMapper::toResponseDTO)
                .toList();
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

    @Transactional
    public void rejectContribution(Long contributionId, String reason) {
        var contribution = contributionRepository.findById(contributionId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));
        if (contribution.getPaymentStatus() == Contribution.PaymentStatus.PENDING) {
            contribution.setPaymentStatus(Contribution.PaymentStatus.CANCELED);
            if (!reason.isEmpty()) {
                String currentNote = contribution.getNote() != null ? contribution.getNote() : "";
                contribution.setNote(currentNote + (currentNote.isBlank() ? "" : " ") + "Bị hủy vì " + reason);
            }
            contributionRepository.save(contribution);
            return;
        }
        throw new IllegalArgumentException("Invalid state for rejection or cancellation");
    }

    @Transactional
    public void confirmContributionWithDeptContribution(ContributionDTO contributionDTO) {
        if (contributionDTO.getUserIds() == null || contributionDTO.getUserIds().isEmpty()) {
            throw new IllegalArgumentException("List user is empty");
        }

        if (contributionDTO.getPeriodId() == null) {
            throw new IllegalArgumentException("Must have info about period");
        }

        if (contributionDTO.getTotalAmount() == null || contributionDTO.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        var period = periodRepository.findById(contributionDTO.getPeriodId())
                .orElseThrow(() -> new ResourceNotFoundException("not found period with id: " + contributionDTO.getPeriodId()));

        List<ContributionResponseDTO> results = new ArrayList<>();

        // process each user in list
        for (Long userId : contributionDTO.getUserIds()) {
            try {
                // create dto for each user
                ContributionDTO singleContributionDTO = new ContributionDTO();
                singleContributionDTO.setUserId(userId);
                singleContributionDTO.setPeriodId(contributionDTO.getPeriodId());
                singleContributionDTO.setTotalAmount(contributionDTO.getTotalAmount());
                singleContributionDTO.setNote(contributionDTO.getNote());
                singleContributionDTO.setFundType(contributionDTO.getFundType());

                // check exist contribution
                var existingContributions = contributionRepository.findByUserIdAndPeriodId(userId, contributionDTO.getPeriodId());

                Long contributionId;

                // check invalid of contribution
                Optional<Contribution> validContribution = existingContributions.stream()
                        .filter(c -> c.getPaymentStatus() != Contribution.PaymentStatus.CANCELED)
                        .findFirst();

                if (validContribution.isPresent()) {
                    // if exist but not paid will be updated
                    Contribution existing = validContribution.get();
                    if (existing.getPaymentStatus() == Contribution.PaymentStatus.PENDING) {
                        // Cập nhật thông tin nếu cần
                        existing.setTotalAmount(contributionDTO.getTotalAmount());
                        if (contributionDTO.getFundType() != null && !contributionDTO.getFundType().isEmpty()) {
                            existing.setFundType(FundType.valueOf(contributionDTO.getFundType()));
                        }
                        if (contributionDTO.getNote() != null && !contributionDTO.getNote().isEmpty()) {
                            existing.setNote(contributionDTO.getNote());
                        }
                        contributionRepository.save(existing);
                        contributionId = existing.getId();
                    } else if (existing.getPaymentStatus() == Contribution.PaymentStatus.PAID) {
                        // if already paid, skip
                        log.info("User {} đã đóng quỹ cho kỳ {}/{}. Bỏ qua.", userId, period.getMonth(), period.getYear());
                        continue;
                    } else {
                        contributionId = existing.getId();
                    }
                } else {
                    // if not exist or canceled, create new contribution
                    ContributionResponseDTO createdContribution = createContribution(singleContributionDTO);
                    contributionId = createdContribution.getId();
                }

                // approve
                approveContribution(contributionId);

                // get contribution info
                ContributionResponseDTO approvedContribution = findById(contributionId);
                results.add(approvedContribution);

                log.info("Đã tạo và duyệt đóng quỹ thành công cho user {} kỳ {}/{}",
                        userId, period.getMonth(), period.getYear());

            } catch (Exception e) {
                log.error("Lỗi khi xử lý đóng quỹ cho user {}: {}", userId, e.getMessage());
                throw new RuntimeException("Error runtime: " + e.getMessage(), e);
            }
        }
    }

    public void sendUnpaidCheckinBillNotification() {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        LocalDate today = LocalDate.now();

        List<ContributionDeptDTO> lateRecords = contributionRepository
                .findUnpaidContributionsBefore(today.getMonthValue(), today.getYear())
                .stream()
                .map(row -> {
                    UserResponseDTO user = new UserResponseDTO(
                            (Long) row[0],
                            (String) row[1],
                            (String) row[2],
                            (String) row[3],
                            (String) row[4],
                            (String) row[5],
                            (String) row[6],
                            row[7] != null ? row[7].toString() : null,
                            row[8] != null ? row[8].toString() : null
                    );
                    int month = (Integer) row[9];
                    int year = (Integer) row[10];
                    BigDecimal amount = (BigDecimal) row[11];
                    return new ContributionDeptDTO(user, month, year, amount);
                })
                .toList();

        if (lateRecords.isEmpty()) {
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("🚨 **Danh sách chưa đóng quỹ các tháng trước ").append(today.getMonthValue())
                .append("/").append(today.getYear()).append(" ** 🚨\n\n");
        message.append("| STT | Tên | Tháng/Năm | Số tiền nợ  |\n");
        message.append("|---|---|---|---|\n");

        int index = 1;
        for (ContributionDeptDTO record : lateRecords) {
            message.append("| ").append(index++).append(" | @")
                    .append(record.getUser().email().replace("@", "-")).append(" | ")
                    .append(record.getMonth()).append("/").append(record.getYear()).append(" | ")
                    .append(formatter.format(record.getAmountToPay())).append(" VNĐ").append(" |\n");
        }

        message.append("\nHãy vào [đây](https://fund-manager-client-e1977.web.app/bills) để đóng quỹ nếu có.\n")
                .append("Rất mong mọi người sẽ tuân thủ quy định và đóng đúng hạn!\n")
                .append("Cùng nhau xây dựng môi trường làm việc chuyên nghiệp nhé 💪🏻\n")
                .append("Trân trọng! \n\n")
                .append(" #contribution-statistic ");

        // Gửi thông báo lên ChatOps
        notification.sendNotification(message.toString(), "java");
    }


    public BigDecimal getTotalContributionAmountByPeriod(int year) {
        return contributionRepository.getTotalPaidContributionsByYear(year);
    }


}
