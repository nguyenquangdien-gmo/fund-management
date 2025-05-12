package com.huybq.fund_management.domain.pen_bill;

import com.huybq.fund_management.domain.balance.BalanceService;
import com.huybq.fund_management.domain.contributions.Contribution;
import com.huybq.fund_management.domain.invoice.InvoiceType;
import com.huybq.fund_management.domain.penalty.Penalty;
import com.huybq.fund_management.domain.penalty.PenaltyDTO;
import com.huybq.fund_management.domain.penalty.PenaltyRepository;
import com.huybq.fund_management.domain.penalty.PenaltyService;
import com.huybq.fund_management.domain.trans.Trans;
import com.huybq.fund_management.domain.trans.TransDTO;
import com.huybq.fund_management.domain.trans.TransRepository;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserMapper;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.domain.user.UserResponseDTO;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import com.huybq.fund_management.utils.chatops.Notification;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
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
    private final UserMapper userMapper;

    public List<PenBillDTO> getAllBillsUnPaidByUserId(Long userId) {
        List<PenBill> penBills = penBillRepository.findByUserIdAndPaymentStatus(userId, PenBill.Status.UNPAID);
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

    public List<PenBillResponse> getAllPenBills() {
        return penBillRepository.findByPaymentStatusInOrderByCreatedAtDesc(List.of(PenBill.Status.PENDING, PenBill.Status.UNPAID, PenBill.Status.CANCELED)).stream()
                .map(mapper::toPenBillResponse)
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
        createTrans(penBill, "Thành viên " + penBill.getUser().getFullName() + " đã thanh toán khoản phạt " + penBill.getPenalty().getName());
    }

    public void rejectPenBill(Long id, String reason) {
        PenBill penBill = penBillRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PenBill not found with ID: " + id));

        if (penBill.getPaymentStatus() == PenBill.Status.CANCELED) {
            throw new IllegalStateException("PenBill is already cancelled.");
        }
        PenBill newPenBill = PenBill.builder()
                .user(penBill.getUser())
                .penalty(penBill.getPenalty())
                .totalAmount(penBill.getTotalAmount())
                .description(penBill.getDescription())
                .paymentStatus(PenBill.Status.UNPAID)
                .dueDate(penBill.getDueDate())
                .build();
        penBill.setPaymentStatus(PenBill.Status.CANCELED);
        if (!reason.isEmpty()) {
            String currentNote = penBill.getDescription() != null ? penBill.getDescription() : "";
            penBill.setDescription(currentNote + (currentNote.isBlank() ? "" : " ") + "Bị hủy vì " + reason);
        }
        penBillRepository.save(penBill);

        penBillRepository.save(newPenBill);
        createTrans(penBill, "Hủy hóa đơn phạt " + penBill.getPenalty().getName() + " của " + penBill.getUser().getFullName() + " vì " + reason);
    }

    private void createTrans(PenBill penBill, String description) {
        Trans transaction = new Trans();
        transaction.setCreatedBy(penBill.getUser());
        transaction.setAmount(penBill.getTotalAmount());
        transaction.setDescription(description);
        transaction.setTransactionType(Trans.TransactionType.INCOME_PENALTY);

        transRepository.save(transaction);
    }

    public void deletePenBill(Long id) {
        if (!penBillRepository.existsById(id)) {
            throw new EntityNotFoundException("PenBill not found with ID: " + id);
        }
        penBillRepository.deleteById(id);
    }

    public void createBill(PenBillDTO penBillDTO) {
        Penalty penalty = penaltyService.getPenaltyBySlug(penBillDTO.getPenaltySlug());

        userRepository.findAllById(penBillDTO.userIds)
                .forEach(user -> {
                    boolean alreadyExists = penBillRepository
                            .findByUserAndPenaltyAndCreatedDate(user.getId(), penalty.getId(), LocalDate.now())
                            .isPresent();

                    if (!alreadyExists) {
                        PenBill penBill = PenBill.builder()
                                .user(user)
                                .penalty(penalty)
                                .totalAmount(penalty.getAmount())
                                .description(penBillDTO.getDescription())
                                .paymentStatus(PenBill.Status.UNPAID)
                                .dueDate(penBillDTO.getDueDate())
                                .build();
                        penBillRepository.save(penBill);
                    }
                });
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

    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Ho_Chi_Minh")
    public void sendNotificationPenBillNew() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        List<Object[]> unpaidInfoList = penBillRepository.findUserAndTotalUnpaidAmountByMonthAndYear(month, year);

        if (unpaidInfoList.isEmpty()) {
            return;
        }

        Map<User, BigDecimal> userToUnpaidMap = new LinkedHashMap<>();
        for (Object[] row : unpaidInfoList) {
            User user = (User) row[0];
            BigDecimal amount = (BigDecimal) row[1];

            userToUnpaidMap.merge(user, amount, BigDecimal::add);
        }

        StringBuilder message = new StringBuilder();
        message.append("🚨 **Danh sách thành viên có hóa đơn phạt chưa thanh toán trong tháng ")
                .append(month).append("/").append(year).append("** 🚨\n\n");
        message.append("| STT | Tên | Số tiền nợ |\n");
        message.append("|---|---|---|\n");

        int index = 1;
        for (Map.Entry<User, BigDecimal> entry : userToUnpaidMap.entrySet()) {
            User user = entry.getKey();
            BigDecimal totalUnpaid = entry.getValue();
            String mention = "@" + user.getEmail().replace("@", "-");

            message.append("| ").append(index++).append(" | ").append(mention).append(" | ")
                    .append(formatCurrency(totalUnpaid)).append(" VNĐ |\n");
        }

        message.append("\nVui lòng vào [đây](https://fund-manager-client-e1977.web.app/bills) để kiểm tra và thanh toán.")
                .append("\nChúng ta cùng nhau xây dựng môi trường làm việc chuyên nghiệp nhé 💪🏻")
                .append("\nTrân trọng!\n\n")
                .append("#unpaid-bills");

        notification.sendNotification(message.toString(), "java");
    }

    public void sendUnpaidCheckinBillNotification() {
        List<PenBillResponse> lateRecords = penBillRepository.findBillsAndTotalUnpaidAmountInDate(LocalDate.now())
                .stream().map(mapper::toPenBillResponse).toList();

        if (lateRecords.isEmpty()) {
            return;
        }

        Map<UserResponseDTO,BigDecimal> userToUnpaid = new LinkedHashMap<>();
        for (PenBillResponse record : lateRecords) {
            UserResponseDTO user = record.getUser();
            BigDecimal amount = record.getAmount();
            userToUnpaid.merge(user, amount, BigDecimal::add);
        }

        StringBuilder message = new StringBuilder();
        message.append("🚨 **Danh sách đi trễ quá số lần cho phép nhưng chưa đóng phạt ").append(" ** 🚨\n\n");
        message.append("| STT | Tên | Số tiền nợ  |\n");
        message.append("|---|---|---|\n");

        int index = 1;
        for (Map.Entry<UserResponseDTO, BigDecimal> unpaidUser : userToUnpaid.entrySet()) {
            UserResponseDTO user = unpaidUser.getKey();
            BigDecimal amount = unpaidUser.getValue();

            message.append("| ").append(index++).append(" | @")
                    .append(user.email().replace("@", "-")).append(" |")
                    .append(formatCurrency(amount)).append(" VN").append(" |\n");
        }

        message.append("\nHãy vào [đây](https://fund-manager-client-e1977.web.app/bills) để đóng phạt nếu có.\n")
                .append("Rất mong mọi người sẽ tuân thủ quy định và đến đúng giờ!\n")
                .append("Hãy cùng nhau xây dựng môi trường làm việc chuyên nghiệp nhé 💪🏻\n")
                .append("Trân trọng! \n\n")
                .append(" #checkin-statistic ");

        // Gửi thông báo lên ChatOps
        notification.sendNotification(message.toString(), "java");
    }

    public Optional<PenBillDTO> findByUserAndPenaltyAndDate(User user, String penSlug, LocalDate date) {
        var penalty = penaltyRepository.findBySlug(penSlug).orElseThrow(()-> new ResourceNotFoundException("Penalty not found with slug: "+penSlug));

        return penBillRepository
                .findByUserAndPenaltyAndCreatedDate(user.getId(), penalty.getId(), date)
                .map(mapper::toDTO);
    }
}
