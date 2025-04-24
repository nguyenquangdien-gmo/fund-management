package com.huybq.fund_management.domain.restaurant;

public class RestaurantMapper {

    public static Restaurant toEntity(RestaurantRequestDTO dto) {
        return Restaurant.builder()
                .name(dto.getName())
                .link(dto.getLink())
                .build();
    }

    public static RestaurantResponseDTO toResponseDTO(Restaurant restaurant) {
        return RestaurantResponseDTO.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .link(restaurant.getLink())
                .isBlacklisted(restaurant.isBlacklisted())
                .orderCount(restaurant.getOrderCount())
                .totalVotes(restaurant.getTotalVotes())
                .totalStars(restaurant.getTotalStars())
                .build();
    }
}
