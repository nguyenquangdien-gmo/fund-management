package com.huybq.fund_management.domain.order_feedback;

import com.huybq.fund_management.domain.order_item.OrderItem;
import com.huybq.fund_management.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface OrderItemVoteRepository extends JpaRepository<OrderItemVote, Long> {

    // Tìm vote theo user và OrderItem
    Optional<OrderItemVote> findByUserAndOrderItem(User user, OrderItem orderItem);

    // Lấy tất cả vote của một OrderItem
    List<OrderItemVote> findByOrderItem(OrderItem orderItem);
}

