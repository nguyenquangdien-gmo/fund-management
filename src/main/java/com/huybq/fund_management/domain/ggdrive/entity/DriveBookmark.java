package com.huybq.fund_management.domain.ggdrive.entity;

import com.huybq.fund_management.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "drive_bookmarks")
public class DriveBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookmarkType type;  // 'FILE', 'FOLDER', or 'EXTERNAL'

    @Column(name = "google_id")
    private String googleId;  // Can be null for external bookmarks

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookmarkSource source; // 'DRIVE' or 'EXTERNAL'

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum BookmarkType {
        FILE, FOLDER, EXTERNAL
    }

    public enum BookmarkSource {
        DRIVE, EXTERNAL
    }
}
