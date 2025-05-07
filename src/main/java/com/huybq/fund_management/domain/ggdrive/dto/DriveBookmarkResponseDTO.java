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
public class DriveBookmarkResponseDTO {

    private Long id;
    private String name;
    private String url;
    private String type;
    private String googleId;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long folderId;
    private String folderName;
    private String createdByUsername;
}
