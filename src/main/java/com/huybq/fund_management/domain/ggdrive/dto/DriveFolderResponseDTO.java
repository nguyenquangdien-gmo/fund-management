package com.huybq.fund_management.domain.ggdrive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriveFolderResponseDTO {

    private Long id;
    private String name;
    private String googleId;
    private String webViewLink;
    private Long parentFolderId;
    private String parentFolderName;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Method to update Google Folder ID directly
    public void setGoogleFolderId(String googleFolderId) {
        this.googleId = googleFolderId;
    }
}
