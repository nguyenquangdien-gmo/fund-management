package com.huybq.fund_management.domain.pen_bill;

import com.huybq.fund_management.domain.balance.BalanceService;
import com.huybq.fund_management.domain.contributions.Contribution;
import com.huybq.fund_management.domain.penalty.Penalty;
import com.huybq.fund_management.domain.penalty.PenaltyRepository;
import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PenBillService {
    private final PenBillRepository penBillRepository;
    private final UserRepository userRepository;
    private final PenaltyRepository penaltyRepository;
    private final BalanceService balanceService;
    private final PenBillMapper mapper;

    public List<PenBillDTO> getAllBillsByUserId(Long userId) {
        List<PenBill> penBills = penBillRepository.findByUserId(userId);
        if (penBills.isEmpty()) {
            throw new EntityNotFoundException("No bills found for user ID: " + userId);
        }
        return penBills.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<PenBillDTO> getAllPenBills() {
        return penBillRepository.findAll().stream()
                .map(mapper::toDTO)
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
        penBill.setPaymentStatus(PenBill.Status.PENDING);
        penBill.setTotalAmount(penalty.getAmount());

        mapper.toDTO(penBillRepository.save(penBill));
    }

    public PenBillDTO updatePenBill(Long id, @Valid PenBillDTO penBillDTO) {
        balanceService.depositBalance("common_fund", penBillDTO.getAmount());
        return penBillRepository.findById(id)
                .map(existingPenBill -> {
                    existingPenBill.setDueDate(penBillDTO.getDueDate());
                    existingPenBill.setPaymentStatus(PenBill.Status.PAID);
                    existingPenBill.setDescription(penBillDTO.getDescription());
                    return mapper.toDTO(penBillRepository.save(existingPenBill));
                })
                .orElseThrow(() -> new EntityNotFoundException("PenBill not found with ID: " + id));

    }

    public void deletePenBill(Long id) {
        if (!penBillRepository.existsById(id)) {
            throw new EntityNotFoundException("PenBill not found with ID: " + id);
        }
        penBillRepository.deleteById(id);
    }

}
