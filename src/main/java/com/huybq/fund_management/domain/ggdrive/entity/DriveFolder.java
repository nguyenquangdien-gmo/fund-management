package com.huybq.fund_management.domain.ggdrive.entity;

import com.huybq.fund_management.domain.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "drive_folders")
public class DriveFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_folder_id", unique = true, nullable = false)
    private String googleFolderId;

    @Column(nullable = false)
    private String name;

    @Column(name = "web_view_link")
    private String webViewLink;

    @CreationTimestamp
    private LocalDateTime createdTime;

    @UpdateTimestamp
    private LocalDateTime modifiedTime;

    @ManyToOne
    @JoinColumn(name = "parent_folder_id")
    private DriveFolder parentFolder;

    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL)
    private List<DriveFolder> subFolders = new ArrayList<>();

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL)
    private List<DriveFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL)
    private List<DriveBookmark> bookmarks = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Long teamId; // ID of the team that owns this folder

}
