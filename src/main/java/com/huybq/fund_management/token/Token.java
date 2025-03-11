package com.huybq.fund_management.token;

import com.huybq.fund_management.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(nullable = false, unique = true)
    private String token;
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;
    @Column(nullable = false)
    private boolean expired;
    @Column(nullable = false)
    private boolean revoked;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
