package com.huybq.fund_management.domain.order_item_feedback;

import com.huybq.fund_management.domain.order_item.OrderItem;
import com.huybq.fund_management.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_item_feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "feedback_type", nullable = false)
    private int feedbackType; // 1 = like, -1 = dislike

    @CreationTimestamp
    @Column(name = "feedback_at", nullable = false, updatable = false)
    private LocalDateTime feedbackAt;
}

