package com.huybq.fund_management.domain.order;

import com.huybq.fund_management.domain.user.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class OrderMapper {

    private final UserMapper mapper;

    public OrderResponseDto toDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setTitle(order.getTitle());
        dto.setDescription(order.getDescription());
        dto.setDeadline(order.getDeadline());
        dto.setStatus(order.getStatus().name());
        dto.setRestaurantId(order.getRestaurant().getId());
        dto.setRestaurantName(order.getRestaurant().getName());
        dto.setCreatedBy(mapper.toResponseDTO(order.getCreatedBy()));
        dto.setCreatedAt(order.getCreatedAt());

        return dto;
    }
}
