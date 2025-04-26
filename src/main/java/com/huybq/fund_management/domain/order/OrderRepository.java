package com.huybq.fund_management.domain.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByRestaurantId(Long restaurantId);
    List<Order> findAllByStatusAndDeadlineBefore(String status, LocalDateTime deadline);
    List<Order> findAllByDeadlineBetween(LocalDateTime start, LocalDateTime end);
}

