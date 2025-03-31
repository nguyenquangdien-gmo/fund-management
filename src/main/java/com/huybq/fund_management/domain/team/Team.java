package com.huybq.fund_management.domain.team;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huybq.fund_management.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Team {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;
    private String name;

    @Column(unique = true)
    private String slug;
    private String channelId;

    @Lob
    private byte[] qrCode;
    private String token;

    @JsonIgnore
    @OneToMany(mappedBy = "team")
    private List<User> members;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
