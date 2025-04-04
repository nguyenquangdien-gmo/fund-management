package com.huybq.fund_management.domain.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByUserId(Long userId);

    @Query("SELECT e FROM Invoice e WHERE FUNCTION('YEAR', e.createdAt) = :year AND FUNCTION('MONTH', e.createdAt) = :month AND e.invoiceType = :invoiceType AND e.status = :status")
    List<Invoice> findByMonthAndYearAndTypeAndStatus(@Param("month") int month, @Param("year") int year, @Param("invoiceType") InvoiceType invoiceType, @Param("status") InvoiceStatus status);

    // Tổng số tiền đã chi hoặc thu nhập theo tháng/năm
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Invoice e WHERE FUNCTION('YEAR', e.createdAt) = :year AND FUNCTION('MONTH', e.createdAt) = :month AND e.invoiceType = :invoiceType AND e.status = :status")
    BigDecimal getTotalByMonthAndYearAndTypeAndStatus(@Param("month") int month, @Param("year") int year, @Param("invoiceType") InvoiceType invoiceType, @Param("status") InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Invoice e WHERE FUNCTION('YEAR', e.createdAt) = :year AND e.invoiceType = :invoiceType AND e.status = :status")
    BigDecimal getTotalByYearAndTypeAndStatus(@Param("year") int year, @Param("invoiceType") InvoiceType invoiceType, @Param("status") InvoiceStatus status);


    List<Invoice> findAllByInvoiceTypeAndStatus(InvoiceType invoiceType, InvoiceStatus status);

    List<Invoice> findAllByStatusOrderByCreatedAtDesc(InvoiceStatus status);

    List<Invoice> findAllByUser_IdOrderByCreatedAtDesc(Long userId);

    List<Invoice> findAllByStatusInOrderByCreatedAtDesc(List<InvoiceStatus> status);
}
