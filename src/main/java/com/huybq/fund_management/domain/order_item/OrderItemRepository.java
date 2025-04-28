package com.huybq.fund_management.domain.order_item;

import com.huybq.fund_management.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByUserId(Long userId);

    boolean existsByOrderIdAndUserId(Long orderId, Long userId);

    List<OrderItem> findByOrder(Order order);


}

