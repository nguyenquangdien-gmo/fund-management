package com.huybq.fund_management.domain.order_item;

import com.huybq.fund_management.domain.order.Order;
import com.huybq.fund_management.domain.order.OrderRepository;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderItemMapper orderItemMapper;

    @Transactional
    public OrderItemResponseDTO createItem(Long userId, OrderItemRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        OrderItem orderItem = OrderItem.builder()
                .user(user)
                .order(order)
                .itemName(request.getItemName())
                .size(request.getSize())
                .sugar(request.getSugar())
                .ice(request.getIce())
                .topping(request.getTopping())
                .note(request.getNote())
                .createdAt(LocalDateTime.now())
                .build();

        orderItem = orderItemRepository.save(orderItem);
        return orderItemMapper.toResponseDTO(orderItem);
    }

    @Transactional
    public List<OrderItemResponseDTO> getItemsByOrder(Long orderId) {
        // Check order tồn tại chưa
        if (!orderRepository.existsById(orderId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        return orderItems.stream()
                .map(orderItemMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public OrderItemResponseDTO getItemById(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found"));

        return orderItemMapper.toResponseDTO(orderItem);
    }

    @Transactional
    public OrderItemResponseDTO updateItem(Long orderItemId, Long userId, OrderItemRequestDTO request) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found"));

        if (!orderItem.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not allowed to update this item");
        }

        orderItem.setItemName(request.getItemName());
        orderItem.setSize(request.getSize());
        orderItem.setSugar(request.getSugar());
        orderItem.setIce(request.getIce());
        orderItem.setTopping(request.getTopping());
        orderItem.setNote(request.getNote());

        orderItem = orderItemRepository.save(orderItem);
        return orderItemMapper.toResponseDTO(orderItem);
    }


}
