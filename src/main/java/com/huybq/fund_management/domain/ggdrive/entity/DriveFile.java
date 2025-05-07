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
@Table(name = "drive_files")
public class DriveFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "google_file_id", nullable = false)
    private String googleFileId;

    @Column(name = "mime_type")
    private String mimeType;

    private Long size;

    @Column(name = "web_view_link")
    private String webViewLink;

    @Column(name = "web_content_link")
    private String webContentLink;

    @ManyToOne
    @JoinColumn(name = "folder_id", nullable = false)
    private DriveFolder folder;

    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
