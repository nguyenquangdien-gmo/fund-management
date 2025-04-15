package com.huybq.fund_management.domain.pen_bill;

import com.huybq.fund_management.domain.balance.BalanceService;
import com.huybq.fund_management.domain.penalty.Penalty;
import com.huybq.fund_management.domain.penalty.PenaltyDTO;
import com.huybq.fund_management.domain.penalty.PenaltyRepository;
import com.huybq.fund_management.domain.penalty.PenaltyService;
import com.huybq.fund_management.domain.trans.Trans;
import com.huybq.fund_management.domain.trans.TransRepository;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.utils.chatops.Notification;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PenBillService {
    private final PenBillRepository penBillRepository;
    private final UserRepository userRepository;
    private final PenaltyRepository penaltyRepository;
    private final TransRepository transRepository;
    private final BalanceService balanceService;
    private final PenaltyService penaltyService;
    private final PenBillMapper mapper;
    private final Notification notification;

    public List<PenBillDTO> getAllBillsUnPaidByUserId(Long userId) {
        List<PenBill> penBills = penBillRepository.findByUserIdAndPaymentStatus(userId,PenBill.Status.UNPAID);
        return penBills.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    public List<PenBillDTO> getAllBillsByUserId(Long userId) {
        List<PenBill> penBills = penBillRepository.findByUserId(userId);
//        if (penBills.isEmpty()) {
//            throw new EntityNotFoundException("No bills found for user ID: " + userId);
//        }
        return penBills.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<PenBillDTO> getAllPenBills() {
        return penBillRepository.findByPaymentStatusInOrderByCreatedAtDesc(List.of(PenBill.Status.PENDING, PenBill.Status.UNPAID, PenBill.Status.CANCELED)).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    public List<PenBillResponse> getPenBillsPending() {
        return penBillRepository.findAllOrderByStatusPriority().stream()
                .map(mapper::toPenBillResponse)
                .collect(Collectors.toList());
    }

    public PenBillDTO getPenBillById(Long id) {
        PenBill penBill = penBillRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PenBill not found with ID: " + id));
        return mapper.toDTO(penBill);
    }

//    public boolean existsByUserIdAndPenaltyId(Long userId, Long penaltyId) {
//        return penBillRepository.existsByUserIdAndPenaltyId(userId, penaltyId);
//    }

    public void createPenBill(@Valid PenBillDTO penBillDTO) {
        User user = userRepository.findById(penBillDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + penBillDTO.getUserId()));

        Penalty penalty = penaltyRepository.findById(penBillDTO.getPenaltyId())
                .orElseThrow(() -> new EntityNotFoundException("Penalty not found with ID: " + penBillDTO.getPenaltyId()));

        PenBill penBill = new PenBill();
        penBill.setUser(user);
        penBill.setPenalty(penalty);
        penBill.setDueDate(penBillDTO.getDueDate());
        penBill.setDescription(penBillDTO.getDescription());
        penBill.setPaymentStatus(PenBill.Status.UNPAID);
        penBill.setTotalAmount(penalty.getAmount());

        mapper.toDTO(penBillRepository.save(penBill));
    }

    public PenBillDTO updatePenBill(Long id) {
        return penBillRepository.findById(id)
                .map(existingPenBill -> {
                    existingPenBill.setPaymentStatus(PenBill.Status.PENDING);
                    return mapper.toDTO(penBillRepository.save(existingPenBill));
                })
                .orElseThrow(() -> new EntityNotFoundException("PenBill not found with ID: " + id));

    }
    public void approvePenBill(Long id) {
        PenBill penBill = penBillRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PenBill not found with ID: " + id));

        if (penBill.getPaymentStatus() == PenBill.Status.PAID) {
            throw new IllegalStateException("PenBill is already approved.");
        }

        // Cập nhật trạng thái PAID
        penBill.setPaymentStatus(PenBill.Status.PAID);
        penBillRepository.save(penBill);

        // Cộng tiền vào common_fund
        balanceService.depositBalance("common", penBill.getTotalAmount());

        // Ghi log giao dịch vào bảng Trans
        Trans transaction = new Trans();
        transaction.setCreatedBy(penBill.getUser());
        transaction.setAmount(penBill.getTotalAmount());
        transaction.setDescription("Thành viên: " + penBill.getUser().getFullName() + ", đóng phạt "+penBill.getPenalty().getName());
        transaction.setTransactionType(Trans.TransactionType.INCOME_PENALTY);

        transRepository.save(transaction);
    }


    public void rejectPenBill(Long id) {
        PenBill penBill = penBillRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PenBill not found with ID: " + id));

        if (penBill.getPaymentStatus() == PenBill.Status.CANCELED) {
            throw new IllegalStateException("PenBill is already cancelled.");
        }


        // Cập nhật trạng thái hủy
        createPenBill(mapper.toDTO(penBill));
        penBill.setPaymentStatus(PenBill.Status.CANCELED);
        penBillRepository.save(penBill);
    }


    public void deletePenBill(Long id) {
        if (!penBillRepository.existsById(id)) {
            throw new EntityNotFoundException("PenBill not found with ID: " + id);
        }
        penBillRepository.deleteById(id);
    }

    public void createLatePenalty(User user) {
        PenaltyDTO penalty = penaltyService.getPenaltyBySlug("late-contribution");

        PenBillDTO penBillDTO = PenBillDTO.builder()
                .userId(user.getId())
                .penaltyId(penalty.getId())
                .amount(penalty.getAmount())
                .dueDate(LocalDate.now())
                .description(penalty.getDescription())
                .build();
        createPenBill(penBillDTO);
    }

    // 1. Thống kê tổng tiền phạt theo từng tháng trong năm
    public List<Map<String, Object>> getMonthlyPenaltyStats(int year) {
        List<Object[]> results = penBillRepository.getMonthlyPenaltyStatistics(year);
        return results.stream()
                .map(result -> Map.of(
                        "month", result[0],
                        "totalAmount", result[1]
                ))
                .collect(Collectors.toList());
    }

    // 2. Tổng số tiền phạt đã thanh toán trong một năm
    public BigDecimal getTotalPaidPenaltiesByYear(int year) {
        return penBillRepository.getTotalPaidPenaltiesByYear(year);
    }

    // 3. Thống kê tổng tiền phạt theo từng năm
    public BillStatisticsDTO getPenaltyStatsByYear(int year) {
        return penBillRepository.getPenaltyStatisticsByYear(year);
    }


    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Ho_Chi_Minh")
    public void sendNotificationPenBill() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        List<Object[]> unpaidInfoList = penBillRepository.findUserAndTotalUnpaidAmountByMonthAndYear(month, year);

        for (Object[] row : unpaidInfoList) {
            User user = (User) row[0];
            BigDecimal totalUnpaid = (BigDecimal) row[1];

            String mention = "@" + user.getEmail().replace("@", "-");

            String message = mention +
                    "\n💸 Bạn có hóa đơn phạt chưa thanh toán!" +
                    "\n🗓 Vào ngày: " + month + "/" + year +
                    "\n💰 Số tiền: " + totalUnpaid + " VNĐ";

            notification.sendNotification(message, "java");
        }
    }

}
