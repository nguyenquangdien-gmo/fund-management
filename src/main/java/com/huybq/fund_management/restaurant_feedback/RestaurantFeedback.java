package com.huybq.fund_management.restaurant_feedback;

import com.huybq.fund_management.domain.restaurant.Restaurant;
import com.huybq.fund_management.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "restaurant_feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // 1 = like, -1 = dislike
    @Column(name = "feedback_type", nullable = false)
    private int feedbackType;

    @Column(name = "feedback_at", nullable = false)
    private LocalDateTime feedbackAt = LocalDateTime.now();
}

