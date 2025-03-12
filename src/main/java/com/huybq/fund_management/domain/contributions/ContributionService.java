package com.huybq.fund_management.domain.contributions;

import com.huybq.fund_management.domain.fund.FundRepository;
import com.huybq.fund_management.domain.pen_bill.PenBillRepository;
import com.huybq.fund_management.domain.penalty.PenaltyRepository;
import com.huybq.fund_management.domain.period.Period;
import com.huybq.fund_management.domain.period.PeriodRepository;
import com.huybq.fund_management.domain.trans.Trans;
import com.huybq.fund_management.domain.trans.TransDTO;
import com.huybq.fund_management.domain.trans.TransService;
import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContributionService {
    private final ContributionRepository contributionRepository;
    private final UserRepository userRepository;
    private final PeriodRepository periodRepository;
    private final PenaltyRepository penaltyRepository;
    private final PenBillRepository penBillRepository;
    private final TransService transService;
    private final FundRepository fundRepository;
    private final ContributionMapper mapper;

    public List<ContributionResponseDTO> findAllContributions(Long periodId) {
        List<Contribution> contributions = contributionRepository.findAllByPeriodId(periodId);

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

    @Transactional
    public ContributionResponseDTO createContribution(ContributionDTO contributionDTO) {
        var user = userRepository.findById(contributionDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var period = periodRepository.findById(contributionDTO.getPeriodId())
                .orElseThrow(() -> new ResourceNotFoundException("Period not found"));

        BigDecimal totalPeriodAmount = period.getTotalAmount(); // 150k mỗi kỳ

        // Kiểm tra người dùng đã có contribution trong period chưa
        var existingContribution = contributionRepository.findByUserIdAndPeriodId(contributionDTO.getUserId(), contributionDTO.getPeriodId());

        if (existingContribution.isPresent()) {
            Contribution contribution = existingContribution.get();
            BigDecimal remainingAmount = totalPeriodAmount.subtract(contribution.getTotalAmount());

            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("You have already fully contributed for this period");
            }

            BigDecimal amountToAdd = contributionDTO.getTotalAmount().min(remainingAmount);
            contribution.setTotalAmount(contribution.getTotalAmount().add(amountToAdd));
            // Cập nhật trạng thái
            if (contribution.getTotalAmount().compareTo(totalPeriodAmount) >= 0) {
                contribution.setPaymentStatus(Contribution.PaymentStatus.PAID);
            } else {
                contribution.setPaymentStatus(Contribution.PaymentStatus.PARTIAL);
            }

            contributionRepository.save(contribution);
            transService.createTransaction(
                    TransDTO.builder()
                            .userId(user.getId())
                            .periodId(period.getId())
                            .transactionType("INCOME_FUND")
                            .amount(contributionDTO.getTotalAmount())
                            .description(contributionDTO.getNote())
                            .build()
            );
            return mapper.mapToResponseDTO(contribution);
        }

        // Nếu chưa có Contribution, tạo mới
        Contribution newContribution = Contribution.builder()
                .user(user)
                .period(period)
                .totalAmount(contributionDTO.getTotalAmount())
                .note(contributionDTO.getNote())
                .build();
        if (contributionDTO.getTotalAmount().compareTo(totalPeriodAmount) < 0) {
            newContribution.setPaymentStatus(Contribution.PaymentStatus.PARTIAL);
        }
        contributionRepository.save(newContribution);
        transService.createTransaction(
                TransDTO.builder()
                        .userId(user.getId())
                        .periodId(period.getId())
                        .transactionType("INCOME_FUND")
                        .amount(contributionDTO.getTotalAmount())
                        .description(contributionDTO.getNote())
                        .build());

        return mapper.mapToResponseDTO(newContribution);
    }

    @Transactional
    public ContributionResponseDTO updateContribution(Long id, ContributionDTO contributionDTO) {
        // Tìm contribution cần cập nhật
        Contribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));

        // Tìm user và period
        User user = userRepository.findById(contributionDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Period period = periodRepository.findById(contributionDTO.getPeriodId())
                .orElseThrow(() -> new ResourceNotFoundException("Period not found"));

        // Tổng số tiền phải đóng theo period
        BigDecimal totalPeriodAmount = period.getTotalAmount();

        // Tổng số tiền đã đóng trước đó
        BigDecimal totalPaid = contribution.getTotalAmount();

        // Tính số tiền còn lại cần đóng
        BigDecimal remainingAmount = totalPeriodAmount.subtract(totalPaid);

        // Nếu đã đóng đủ thì không cho cập nhật
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("You have already fully contributed for this period");
        }

        // Xác định số tiền mới được thêm vào
        BigDecimal amountToAdd = contributionDTO.getTotalAmount();
        if (amountToAdd.compareTo(remainingAmount) > 0) {
            amountToAdd = remainingAmount; // Không cho đóng vượt mức
        }

        // Cập nhật tổng số tiền đóng góp
        contribution.setTotalAmount(totalPaid.add(amountToAdd));
        contribution.setNote(contributionDTO.getNote());

        // Cập nhật trạng thái
        if (contribution.getTotalAmount().compareTo(totalPeriodAmount) >= 0) {
            contribution.setPaymentStatus(Contribution.PaymentStatus.PAID);
        } else {
            contribution.setPaymentStatus(Contribution.PaymentStatus.PARTIAL);
        }
        transService.createTransaction(
                TransDTO.builder()
                        .userId(user.getId())
                        .periodId(period.getId())
                        .transactionType("INCOME_FUND")
                        .amount(contributionDTO.getTotalAmount())
                        .description(contributionDTO.getNote())
                        .build());
        // Lưu vào DB
        contributionRepository.save(contribution);
        return mapper.mapToResponseDTO(contribution);
    }

    private void createFundTransactions(Contribution contribution) {

    }

    private void createContribution(Contribution contribution, Trans FundTransaction) {

    }

    private void createLatePenalty(User member, Period period, LocalDate now) {


    }


}
