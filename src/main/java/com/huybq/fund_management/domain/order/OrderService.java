package com.huybq.fund_management.domain.order;

import com.huybq.fund_management.domain.chatopsApi.ChatopsService;
import com.huybq.fund_management.domain.restaurant.Restaurant;
import com.huybq.fund_management.domain.restaurant.RestaurantRepository;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.utils.chatops.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Autowired
    private Notification notification;

    @Autowired
    private OrderMapper orderMapper;


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

        // Cập nhật số lượng đơn hàng của nhà hàng
        restaurant.setOrderCount(restaurant.getOrderCount() + 1);

        // Gửi thông báo đến nhóm chat
        sendNotificationNewOrder(request, order);

        // Trả về OrderResponseDto sau khi đã tạo xong đơn hàng
        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        // Lấy tất cả các đơn hàng
        List<Order> orders = orderRepository.findAll();

        // Map các đơn hàng thành OrderResponseDto và trả về
        return orders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    public void sendNotificationNewOrder(OrderRequestDto order, Order orderDetail) {
        // Lấy thông tin từ order
        String title = order.getTitle();
        String description = order.getDescription();
        List<Long> relatedUsersIds = order.getRelatedUserIds();
        List<User> relatedUsers = userRepository.findAllById(relatedUsersIds);

        StringBuilder message = new StringBuilder();
        message.append("@all\n");
        message.append("**").append(title).append("**\n\n");

        if (description != null && !description.isEmpty()) {
            message.append(description).append("\n\n");
        }

        // Thêm tag cho các thành viên liên quan
        if (relatedUsers != null && !relatedUsers.isEmpty()) {
            for (User user : relatedUsers) {
                String mention = "@" + user.getEmail().replace("@", "-");
                message.append(mention).append(" ");
            }
            message.append("\n");
        }

        // Thêm link tới trang orders
        message.append("Anh/chị/em hãy order tại [đây](https://fund-manager-client-e1977.web.app/orders/")
                .append(orderDetail.getId())
                .append(")");

        // Gửi thông báo
        notification.sendNotification(message.toString(), "java");
    }

    public List<OrderResponseDto> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders;
        if (startDate != null && endDate != null) {
            orders = orderRepository.findAllByDeadlineBetween(startDate, endDate);
        } else {
            orders = orderRepository.findAll();
        }
        return orders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    public OrderResponseDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return orderMapper.toDto(order);
    }
}

