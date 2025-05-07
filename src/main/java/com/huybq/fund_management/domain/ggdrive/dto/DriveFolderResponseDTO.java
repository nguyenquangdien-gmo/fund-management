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
    private String webViewLink;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    private Long parentFolderId;
    private String parentFolderName;
    private String createdByUsername;
}
