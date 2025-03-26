package com.huybq.fund_management.domain.pen_bill;

import com.huybq.fund_management.domain.contributions.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.util.Collection;
import java.util.List;

@Service
public interface PenBillRepository extends JpaRepository<PenBill, Long> {
    List<PenBill> findByUserId(Long userId);
    List<PenBill> findByUserIdAndPaymentStatus(Long userId, PenBill.Status status);

    List<PenBill> findByPaymentStatusIn(List<PenBill.Status> statuses);
}
