package com.huybq.fund_management.domain.restaurant;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantRequestDTO {
    private String name;
    private String link;
}