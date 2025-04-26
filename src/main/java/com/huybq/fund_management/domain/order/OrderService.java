package com.huybq.fund_management.domain.order;

import com.huybq.fund_management.domain.restaurant.Restaurant;
import com.huybq.fund_management.domain.restaurant.RestaurantRepository;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public OrderResponseDto createOrder(Long userId, OrderRequestDto request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tạo đơn hàng mới
        Order order = Order.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .status(Order.Status.ORDERING)
                .restaurant(restaurant)
                .createdBy(creator)
                .build();

        // Lưu đơn hàng vào database
        order = orderRepository.save(order);

        // Trả về OrderResponseDto sau khi đã tạo xong đơn hàng
        return OrderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        // Lấy tất cả các đơn hàng
        List<Order> orders = orderRepository.findAll();

        // Map các đơn hàng thành OrderResponseDto và trả về
        return orders.stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }
}

