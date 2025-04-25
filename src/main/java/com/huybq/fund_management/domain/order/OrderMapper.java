package com.huybq.fund_management.domain.order;

public class OrderMapper {

    public static OrderResponseDto toDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setTitle(order.getTitle());
        dto.setDescription(order.getDescription());
        dto.setDeadline(order.getDeadline());
        dto.setStatus(order.getStatus().name());
        dto.setRestaurantId(order.getRestaurant().getId());
        dto.setRestaurantName(order.getRestaurant().getName());
        dto.setCreatedBy(order.getCreatedBy().getId());
        dto.setCreatedAt(order.getCreatedAt());

        return dto;
    }
}
