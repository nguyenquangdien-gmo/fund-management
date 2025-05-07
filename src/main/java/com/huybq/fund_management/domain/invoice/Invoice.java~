package com.huybq.fund_management.domain.invoice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.huybq.fund_management.domain.fund.FundType;
import com.huybq.fund_management.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Invoice {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "user_id")
    private User user;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private FundType fundType;

    private InvoiceType invoiceType;

    private InvoiceStatus status;

    @Lob
    private byte[] billImage;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
