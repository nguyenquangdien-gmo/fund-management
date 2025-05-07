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
public class DriveFileResponseDTO {

    private Long id;
    private String name;
    private String googleId;
    private String mimeType;
    private Long size;
    private String webViewLink;
    private String webContentLink;
    private Long folderId;
    private String folderName;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    private String createdByUsername;
    private String uploadedByUsername;

    // Method to update Google File ID directly
    public void setGoogleFileId(String googleFileId) {
        this.googleId = googleFileId;
    }
}
