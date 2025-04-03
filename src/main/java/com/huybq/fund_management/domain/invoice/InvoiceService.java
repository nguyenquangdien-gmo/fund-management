package com.huybq.fund_management.domain.invoice;

import com.huybq.fund_management.domain.balance.Balance;
import com.huybq.fund_management.domain.balance.BalanceService;
import com.huybq.fund_management.domain.fund.FundType;
import com.huybq.fund_management.domain.trans.Trans;
import com.huybq.fund_management.domain.trans.TransDTO;
import com.huybq.fund_management.domain.trans.TransService;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository repository;
    private final InvoiceMapper mapper;
    private final UserRepository userRepository;
    private final BalanceService balanceService;
    private final TransService transService;

    public List<InvoiceResponseDTO> getInvoices() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    public List<InvoiceResponseDTO> getInvoicesWithStatusPending()  {
        return repository.findAllByStatus(InvoiceStatus.PENDING).stream()
                .map(mapper::toDTO)
                .toList();
    }
    public List<InvoiceResponseDTO> getInvoicesByUserId(Long userId) {
        return repository.findAllByUser_Id(userId).stream()
                .map(mapper::toDTO)
                .toList();
    }

    public BigDecimal getTotalAmount(String invoiceType) {
        return repository.findAllByInvoiceTypeAndStatus(InvoiceType.valueOf(invoiceType.toUpperCase()),InvoiceStatus.APPROVED).stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<InvoiceResponseDTO> getInvoiceByMonthAndYear(String invoiceType,int month, int year) {
        return repository.findByMonthAndYearAndTypeAndStatus(month, year, InvoiceType.valueOf(invoiceType.toUpperCase()),InvoiceStatus.APPROVED).stream()
                .map(mapper::toDTO)
                .toList();
    }

    public BigDecimal getTotalAmountByMonthAndYear(int month, int year, String invoiceType) {
        return repository.getTotalByMonthAndYearAndTypeAndStatus(month, year, InvoiceType.valueOf(invoiceType.toUpperCase()),InvoiceStatus.APPROVED);
    }

    public BigDecimal getTotalAmountByYear(int year,String invoiceType) {
        return repository.getTotalByYearAndTypeAndStatus(year, InvoiceType.valueOf(invoiceType.toUpperCase()),InvoiceStatus.APPROVED);
    }

    @Transactional
    public InvoiceResponseDTO create(InvoiceDTO dto) {
        var user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + dto.userId()));
        var invoice = mapper.toEntity(dto);
        invoice.setUser(user);
        invoice = repository.save(invoice);

        return mapper.toDTO(invoice);
    }

    public InvoiceResponseDTO approve(Long idInvoice) {
        return repository.findById(idInvoice)
                .map(invoice -> {
                    if (invoice.getStatus() == InvoiceStatus.APPROVED) {
                        throw new IllegalStateException("Invoice is already approved.");
                    }
                    balanceService.depositBalance(invoice.getFundType().name(), invoice.getAmount());

                    TransDTO transDTO = TransDTO.builder()
                            .amount(invoice.getAmount())
                            .description("Approved transaction for: " + invoice.getName())
                            .transactionType(invoice.getInvoiceType() == InvoiceType.EXPENSE
                                    ? Trans.TransactionType.EXPENSE
                                    : Trans.TransactionType.INCOME_FUND)
                            .userId(invoice.getUser().getId())
                            .build();

                    transService.createTransaction(transDTO);

                    invoice.setStatus(InvoiceStatus.APPROVED);
                    return mapper.toDTO(repository.save(invoice));
                })
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with ID: " + idInvoice));
    }
    public InvoiceResponseDTO update(Long idInvoice, InvoiceDTO dto) {
        return repository.findById(idInvoice)
                .map(invoice -> {
                    if (invoice.getStatus() == InvoiceStatus.APPROVED) {
                        throw new IllegalStateException("Cannot update an approved invoice.");
                    }

                    var user = userRepository.findById(dto.userId())
                            .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + dto.userId()));

                    invoice.setUser(user);
                    invoice.setName(dto.name());
                    invoice.setAmount(dto.amount());
                    invoice.setDescription(dto.description());
                    invoice.setInvoiceType(InvoiceType.valueOf(dto.invoiceType()));

                    return mapper.toDTO(repository.save(invoice));
                })
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with ID: " + idInvoice));
    }

    public InvoiceResponseDTO reject(Long idInvoice) {
        return repository.findById(idInvoice)
                .map(invoice -> {
                    if (invoice.getStatus() == InvoiceStatus.APPROVED) {
                        throw new IllegalStateException("Cannot reject an approved invoice.");
                    }

                    invoice.setStatus(InvoiceStatus.CANCELLED);
                    return mapper.toDTO(repository.save(invoice));
                })
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with ID: " + idInvoice));
    }



    public void delete(Long idInvoice) {
        repository.deleteById(idInvoice);
    }
}
