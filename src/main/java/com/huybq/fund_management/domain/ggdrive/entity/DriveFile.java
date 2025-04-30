package com.huybq.fund_management.domain.ggdrive.entity;

import com.huybq.fund_management.domain.user.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class DriveFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String googleFileId;
    private String mimeType;
    private Long size;
    private String webViewLink;
    private String webContentLink;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private DriveFolder folder;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @ManyToOne
    @JoinColumn(name = "drive_account_id")
    private GoogleDriveAccount googleDriveAccount;
}