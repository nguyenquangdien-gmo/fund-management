package com.huybq.fund_management.domain.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findAllByIsBlacklistedFalse();
    boolean existsByLink(String link);
}