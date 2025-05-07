package com.huybq.fund_management.domain.order_item;

import com.huybq.fund_management.domain.order.Order;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class OrderItemMapper {

    private final UserMapper userMapper;

    public static OrderItem toEntity(OrderItemRequestDTO dto, Order order, User user) {
        return OrderItem.builder()
                .order(order)
                .user(user)
                .itemName(dto.getItemName())
                .size(dto.getSize())
                .sugar(dto.getSugar())
                .ice(dto.getIce())
                .topping(dto.getTopping())
                .note(dto.getNote())
                .build();
    }

    public OrderItemResponseDTO toResponseDTO(OrderItem item) {
        return OrderItemResponseDTO.builder()
                .id(item.getId())
                .orderId(item.getOrder().getId())
                .user(userMapper.toResponseDTO(item.getUser()))
                .itemName(item.getItemName())
                .size(item.getSize())
                .sugar(item.getSugar())
                .ice(item.getIce())
                .topping(item.getTopping())
                .note(item.getNote())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
