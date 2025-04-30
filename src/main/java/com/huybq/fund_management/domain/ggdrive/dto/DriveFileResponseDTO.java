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
    private String mimeType;
    private Long size;
    private String webViewLink;
    private String webContentLink;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    private Long folderId;
    private String folderName;
    private String createdByUsername;
}
