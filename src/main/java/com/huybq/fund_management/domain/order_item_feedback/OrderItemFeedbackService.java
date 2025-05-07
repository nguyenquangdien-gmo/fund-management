package com.huybq.fund_management.domain.order_item_feedback;

import com.huybq.fund_management.domain.order.Order;
import com.huybq.fund_management.domain.order.OrderRepository;
import com.huybq.fund_management.domain.order_item.OrderItem;
import com.huybq.fund_management.domain.order_item.OrderItemRepository;
import com.huybq.fund_management.domain.restaurant.Restaurant;
import com.huybq.fund_management.domain.restaurant.RestaurantRepository;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.sql.SQLOutput;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderItemFeedbackService {

    private final OrderItemFeedbackRepository feedbackRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;

    public OrderItemFeedbackResponseDTO createFeedback(User user, OrderItemFeedbackRequestDTO request) {
        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found"));

        if(orderItem.getUser().getId().toString() == user.getId().toString()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot give feedback on your own order item");
        }

        // Từ orderItem -> order
        Order order = orderRepository.findById(orderItem.getOrder().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        // Từ order -> restaurant
        Restaurant restaurant = restaurantRepository.findById(order.getRestaurant().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));

        // Tạo feedback
        Optional<OrderItemFeedback> existingFeedbackOpt = feedbackRepository.findByUserAndOrderItem(user, orderItem);

        OrderItemFeedback feedback;
        if (existingFeedbackOpt.isPresent()) {
            // Nếu đã có, update feedbackType
            feedback = existingFeedbackOpt.get();
            feedback.setFeedbackType(request.getFeedbackType());
            feedback.setFeedbackAt(LocalDateTime.now()); // Cập nhật thời gian feedback mới
        } else {
            // Nếu chưa có, tạo mới
            feedback = OrderItemFeedback.builder()
                    .user(user)
                    .orderItem(orderItem)
                    .feedbackType(request.getFeedbackType())
                    .build();
        }

        feedback = feedbackRepository.save(feedback);

        updateRestaurantBlacklist(restaurant);

        return OrderItemFeedbackMapper.toResponseDTO(feedback);
    }

    private void updateRestaurantBlacklist(Restaurant restaurant) {
        Long restaurantId = restaurant.getId();

        // Lấy tất cả order của nhà hàng
        List<Order> orders = orderRepository.findAllByRestaurantId(restaurantId);

        // Gom tất cả các orderItem thuộc các order đó
        List<OrderItem> allOrderItems = new ArrayList<>();
        for (Order order : orders) {
            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
            allOrderItems.addAll(orderItems);
        }

        // Từ list OrderItem, lấy hết feedback
        List<OrderItemFeedback> allFeedbacks = new ArrayList<>();
        for (OrderItem orderItem : allOrderItems) {
            List<OrderItemFeedback> feedbacks = feedbackRepository.findByOrderItem(orderItem);
            allFeedbacks.addAll(feedbacks);
        }

        // Đếm like/dislike
        long likes = allFeedbacks.stream().filter(fb -> fb.getFeedbackType() == 1).count();
        long dislikes = allFeedbacks.stream().filter(fb -> fb.getFeedbackType() == -1).count();

        // Điều kiện blacklist hoặc whitelist
        boolean shouldBeBlacklisted = dislikes > likes;
        boolean shouldBeWhitelisted = likes > dislikes;

        if (shouldBeBlacklisted && !restaurant.isBlacklisted()) {
            restaurant.setBlacklisted(true);
            restaurantRepository.save(restaurant);
        } else if (shouldBeWhitelisted && restaurant.isBlacklisted()) {
            restaurant.setBlacklisted(false);
            restaurantRepository.save(restaurant);
        }
    }

}
