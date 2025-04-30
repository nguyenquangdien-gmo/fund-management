package com.huybq.fund_management.domain.order_item_feedback;

import com.huybq.fund_management.domain.order_item.OrderItem;
import com.huybq.fund_management.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderItemFeedbackRepository extends JpaRepository<OrderItemFeedback, Long> {
    List<OrderItemFeedback> findByOrderItem(OrderItem orderItem);
    Optional<OrderItemFeedback> findByUserAndOrderItem(User user, OrderItem orderItem);
}