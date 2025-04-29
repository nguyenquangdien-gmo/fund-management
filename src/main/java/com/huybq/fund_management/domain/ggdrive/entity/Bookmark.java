package com.huybq.fund_management.domain.ggdrive.entity;

import com.huybq.fund_management.domain.user.User;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Bookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String link;

    @Enumerated(EnumType.STRING)
    private BookmarkType type;

    private String googleId; // Google File or Folder ID

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public enum BookmarkType {
        FILE, FOLDER
    }
}
