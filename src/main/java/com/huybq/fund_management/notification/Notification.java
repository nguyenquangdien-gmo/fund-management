package com.huybq.fund_management.notification;

import com.huybq.fund_management.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String message;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.UNREAD;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
