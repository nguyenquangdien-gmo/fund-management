package com.huybq.fund_management.domain.invoice;

import com.huybq.fund_management.domain.balance.BalanceService;
import com.huybq.fund_management.domain.fund.FundType;
import com.huybq.fund_management.domain.reminder.ReminderDTO;
import com.huybq.fund_management.domain.reminder.ReminderService;
import com.huybq.fund_management.domain.role.Role;
import com.huybq.fund_management.domain.role.RoleRepository;
import com.huybq.fund_management.domain.trans.Trans;
import com.huybq.fund_management.domain.trans.TransDTO;
import com.huybq.fund_management.domain.trans.TransService;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import com.huybq.fund_management.utils.chatops.Notification;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository repository;
    private final InvoiceMapper mapper;
    private final UserRepository userRepository;
    private final BalanceService balanceService;
    private final TransService transService;
    private final ReminderService reminderService;
    private final RoleRepository roleRepository;

    public List<InvoiceResponseDTO> getInvoices() {
        return repository.findAllByStatusInOrderByCreatedAtDesc(List.of(InvoiceStatus.APPROVED)).stream()
                .map(mapper::toDTO)
                .toList();
    }

    public List<InvoiceResponseDTO> getInvoicesWithStatusPending() {
        return repository.findAllOrderByStatusPriority().stream()
                .map(mapper::toDTO)
                .toList();
    }

    public List<InvoiceResponseDTO> getInvoicesByUserId(Long userId) {
        return repository.findAllByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toDTO)
                .toList();
    }

    public BigDecimal getTotalAmount(String invoiceType) {
        return repository.findAllByInvoiceTypeAndStatus(InvoiceType.valueOf(invoiceType.toUpperCase()), InvoiceStatus.APPROVED).stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<InvoiceResponseDTO> getInvoiceByMonthAndYear(String invoiceType, int month, int year) {
        return repository.findByMonthAndYearAndTypeAndStatus(month, year, InvoiceType.valueOf(invoiceType.toUpperCase()), InvoiceStatus.APPROVED).stream()
                .map(mapper::toDTO)
                .toList();
    }

    public BigDecimal getTotalAmountByMonthAndYear(int month, int year, String invoiceType) {
        return repository.getTotalByMonthAndYearAndTypeAndStatus(month, year, InvoiceType.valueOf(invoiceType.toUpperCase()), InvoiceStatus.APPROVED);
    }

    public BigDecimal getTotalAmountByYear(int year, String invoiceType) {
        return repository.getTotalByYearAndTypeAndStatus(year, InvoiceType.valueOf(invoiceType.toUpperCase()), InvoiceStatus.APPROVED);
    }

    public List<Map<String, Object>> getMonthlyInvoiceStats(int year, String type) {
        List<Object[]> results = repository.getMonthlyInvoiceStatistics(year, InvoiceType.valueOf(type.toUpperCase()));
        List<Map<String, Object>> response = new ArrayList<>();

        for (Object[] row : results) {
            if (row.length >= 2) {
                Map<String, Object> data = new HashMap<>();
                data.put("month", row[0]); // Tháng (int)
                data.put("totalAmount", row[1]); // Tổng tiền (BigDecimal)
                response.add(data);
            }
        }

        return response;
    }

    public InvoiceStatsDTO getYearInvoiceStats(int year, String type) {
        return repository.getYearInvoiceStatistics(year, InvoiceType.valueOf(type.toUpperCase()));
    }

    @Transactional
    public InvoiceResponseDTO create(InvoiceDTO dto, MultipartFile billImage) throws IOException {
        var user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + dto.userId()));
        var invoice = mapper.toEntity(dto);

        if (billImage != null && !billImage.isEmpty()) {
            invoice.setBillImage(billImage.getBytes());
        }
        invoice.setUser(user);
        if (billImage != null && !billImage.isEmpty()) {
            invoice.setBillImage(billImage.getBytes());
        }
        invoice = repository.save(invoice);

        // Gửi thông báo
        sendNotificationToAdmins(dto, user);

        return mapper.toDTO(invoice);
    }


    public InvoiceResponseDTO approve(Long idInvoice, String fundType) {
        return repository.findById(idInvoice)
                .map(invoice -> {
                    if (invoice.getStatus() == InvoiceStatus.APPROVED) {
                        throw new IllegalStateException("Invoice is already approved.");
                    }
                    var balance = balanceService.findBalanceByTitle(fundType.toLowerCase());
                    if (balance == null) {
                        throw new EntityNotFoundException("Balance not found with title: " + fundType);
                    }
                    if (invoice.getInvoiceType() == InvoiceType.EXPENSE) {
                        balanceService.withdrawBalance(fundType.toLowerCase(), invoice.getAmount());
                    } else {
                        balanceService.depositBalance(fundType.toLowerCase(), invoice.getAmount());
                    }

                    TransDTO transDTO = TransDTO.builder()
                            .amount(invoice.getAmount())
                            .description("Phê duyệt phiếu: " + invoice.getDescription() + " - " + invoice.getUser().getFullName())
                            .transactionType(invoice.getInvoiceType() == InvoiceType.EXPENSE
                                    ? Trans.TransactionType.EXPENSE
                                    : Trans.TransactionType.INCOME_FUND)
                            .userId(invoice.getUser().getId())
                            .build();

                    transService.createTransaction(transDTO);
                    invoice.setFundType(FundType.valueOf(fundType.toUpperCase()));
                    invoice.setStatus(InvoiceStatus.APPROVED);
                    return mapper.toDTO(repository.save(invoice));
                })
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with ID: " + idInvoice));
    }

    public InvoiceResponseDTO update(Long idInvoice, InvoiceDTO dto, MultipartFile billImage)  throws IOException {
        return repository.findById(idInvoice)
                .map(invoice -> {
                    if (invoice.getStatus() == InvoiceStatus.APPROVED) {
                        throw new IllegalStateException("Cannot update an approved invoice.");
                    }

                    var user = userRepository.findById(dto.userId())
                            .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + dto.userId()));

                    if (billImage != null && !billImage.isEmpty()) {
                        try {
                            invoice.setBillImage(billImage.getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    invoice.setUser(user);
                    invoice.setAmount(dto.amount());

                    if (billImage != null && !billImage.isEmpty()) {
                        try {
                            invoice.setBillImage(billImage.getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    invoice.setDescription(dto.description());
                    invoice.setInvoiceType(InvoiceType.valueOf(dto.invoiceType()));

                    return mapper.toDTO(repository.save(invoice));
                })
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with ID: " + idInvoice));
    }

    public InvoiceResponseDTO reject(Long idInvoice, String reason) {
        return repository.findById(idInvoice)
                .map(invoice -> {
                    if (invoice.getStatus() == InvoiceStatus.APPROVED) {
                        throw new IllegalStateException("Cannot reject an approved invoice.");
                    }
                    TransDTO transDTO = TransDTO.builder()
                            .amount(invoice.getAmount())
                            .description("Hủy phiếu: " + " - " + invoice.getUser().getFullName() + invoice.getDescription() + " - \nlý do: " + reason)
                            .transactionType(invoice.getInvoiceType() == InvoiceType.EXPENSE
                                    ? Trans.TransactionType.EXPENSE
                                    : Trans.TransactionType.INCOME_FUND)
                            .userId(invoice.getUser().getId())
                            .build();

                    transService.createTransaction(transDTO);
                    if (!reason.isEmpty()) {
                        String currentNote = invoice.getDescription() != null ? invoice.getDescription() : "";
                        invoice.setDescription(currentNote + (currentNote.isBlank() ? "" : " ") + "Bị hủy vì " + reason);
                    }
                    invoice.setStatus(InvoiceStatus.CANCELLED);
                    return mapper.toDTO(repository.save(invoice));
                })
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with ID: " + idInvoice));
    }

    public void delete(Long idInvoice) {
        repository.deleteById(idInvoice);
    }

    public byte[] getBillImage(Long invoiceId) {
        var invoice = repository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        return invoice.getBillImage();
    }

    private void sendNotificationToAdmins(InvoiceDTO dto, User user) {
        // Lấy role "ADMIN"
        Optional<Role> adminRole = roleRepository.findByName("ADMIN");

        // Kiểm tra xem có role ADMIN không
        if (adminRole.isPresent()) {
            List<User> allAdmins = userRepository.findAllAdmin(adminRole.get());

            Date date = new Date(); // hoặc biến bạn đã có
            LocalDateTime localDateTime = date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            // Duyệt qua từng admin và gửi thông báo
            for (User admin : allAdmins) {
                ReminderDTO reminderDTO = ReminderDTO.builder()
                        .title("Thông báo từ hệ thống")
                        .description("Có một hóa đơn mới từ " + user.getFullName())
                        .type("OTHER")
                        .scheduledTime(localDateTime)
                        .isSendChatGroup(true)
                        .userIds(List.of(admin.getId()))
                        .build();
                reminderService.createReminder(reminderDTO, admin.getEmail());
            }
        }
    }

//    private void sendMessageToChatOps(InvoiceDTO dto) {
//        StringBuilder messageBuilder = new StringBuilder();
//
//        Optional<Role> adminRole = roleRepository.findByName("ADMIN");
//
//        if (adminRole.isPresent()) {
//            List<User> allAdmins = userRepository.findAllAdmin(adminRole.get());
//            for (User admin : allAdmins) {
//                String mention = "@" + admin.getEmail().replace("@", "-");
//                messageBuilder.append(mention).append(" ");
//            }
//        } else {
//            messageBuilder.append("@all");
//        }
//
//        messageBuilder.append("\n\n");
//
//        if (dto.userId() != null) {
//            Optional<User> relatedUser = userRepository.findById(dto.userId());
//            if (relatedUser.isPresent()) {
//                String mention = "@" + relatedUser.get().getEmail().replace("@", "-");
//                messageBuilder.append(mention).append(" ");
//            }
//            messageBuilder.append("** đã đóng hóa đơn**");
//            messageBuilder.append("\n\n");
//        }
//
//        if (dto.description() != null && !dto.description().isEmpty()) {
//            messageBuilder.append("**Mô tả: **").append(dto.description()).append("\n\n");
//        }
//
//        // Thêm link tới trang orders
//        messageBuilder.append("Hãy xem thông tin tại [đây](https://fund-manager-client-e1977.web.app/invoices/")
//                .append(dto.userId())
//                .append(")");
//
//        String channelId = "java";
//        notification.sendNotification(messageBuilder.toString(), channelId);
//    }
//
//    private void sendNotificationToAdmins(InvoiceDTO dto) {
//        // Lấy role "ADMIN"
//        Optional<Role> adminRole = roleRepository.findByName("ADMIN");
//
//        // Kiểm tra xem có role ADMIN không
//        if (adminRole.isPresent()) {
//            List<User> allAdmins = userRepository.findAllAdmin(adminRole.get());
//
//            // Duyệt qua từng admin và gửi thông báo
//            for (User admin : allAdmins) {
//                StringBuilder messageBuilder = new StringBuilder();
//
//                // Mention admin trong message
//                String mention = "@" + admin.getEmail().replace("@", "-");
//                messageBuilder.append(mention).append(" ");
//
//                messageBuilder.append("\n\n");
//
//                // Thông tin về user đã đóng hóa đơn
//                if (dto.userId() != null) {
//                    Optional<User> relatedUser = userRepository.findById(dto.userId());
//                    if (relatedUser.isPresent()) {
//                        String relatedUserMention = "@" + relatedUser.get().getEmail().replace("@", "-");
//                        messageBuilder.append(relatedUserMention).append(" ");
//                    }
//                    messageBuilder.append("** đã đóng hóa đơn**");
//                    messageBuilder.append("\n\n");
//                }
//
//                // Mô tả hóa đơn
//                if (dto.description() != null && !dto.description().isEmpty()) {
//                    messageBuilder.append("**Mô tả: **").append(dto.description()).append("\n\n");
//                }
//
//                // Thêm link tới trang orders
//                messageBuilder.append("Hãy xem thông tin tại [đây](https://fund-manager-client-e1977.web.app/invoices/")
//                        .append(dto.userId())
//                        .append(")");
//
//                // Gửi thông báo cho admin
//                notification.sendNotificationForMember(messageBuilder.toString(), "system", admin.getEmail());
//            }
//        } else {
//            // Nếu không tìm thấy role ADMIN, gửi thông báo cho một admin mặc định hoặc nhóm
//            StringBuilder defaultMessage = new StringBuilder();
//            defaultMessage.append("@all");
//            notification.sendNotificationForMember(defaultMessage.toString(), "system", "admin@example.com");
//        }
//    }

}
