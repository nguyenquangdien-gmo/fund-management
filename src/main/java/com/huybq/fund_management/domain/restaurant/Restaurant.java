package com.huybq.fund_management.domain.restaurant;

import com.huybq.fund_management.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String link;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private RestaurantType type;


    @Column(name = "is_blacklisted", nullable = false)
    private boolean isBlacklisted = false;

    @Column(name = "order_count", nullable = false)
    private int orderCount = 0;

    @Column(name = "total_votes", nullable = false)
    private int totalVotes = 0;

    @Column(name = "total_stars", nullable = false)
    private int totalStars = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    public enum RestaurantType {
        DRINK,
        FOOD,
        BOTH
    }
}


