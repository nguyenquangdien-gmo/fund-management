package com.huybq.fund_management.domain.ggdrive.mapper;

import com.huybq.fund_management.domain.ggdrive.dto.DriveBookmarkResponseDTO;
import com.huybq.fund_management.domain.ggdrive.dto.DriveFileResponseDTO;
import com.huybq.fund_management.domain.ggdrive.dto.DriveFolderResponseDTO;
import com.huybq.fund_management.domain.ggdrive.entity.DriveBookmark;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFile;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFolder;
import com.huybq.fund_management.domain.ggdrive.service.GoogleDriveService;
import org.springframework.stereotype.Component;

@Component
public class DriveMapper {

    public DriveFileResponseDTO toDriveFileResponseDTO(DriveFile driveFile) {
        String googleId = driveFile.getGoogleFileId();
        if ((googleId == null || googleId.isEmpty()) && driveFile.getWebViewLink() != null) {
            googleId = GoogleDriveService.extractGoogleIdFromUrl(driveFile.getWebViewLink());
        }

        return DriveFileResponseDTO.builder()
                .id(driveFile.getId())
                .name(driveFile.getName())
                .googleId(googleId)
                .mimeType(driveFile.getMimeType())
                .size(driveFile.getSize())
                .webViewLink(driveFile.getWebViewLink())
                .webContentLink(driveFile.getWebContentLink())
                .folderId(driveFile.getFolder() != null ? driveFile.getFolder().getId() : null)
                .folderName(driveFile.getFolder() != null ? driveFile.getFolder().getName() : null)
                .createdTime(driveFile.getCreatedTime())
                .modifiedTime(driveFile.getModifiedTime())
                .createdByUsername(driveFile.getCreatedBy() != null ? driveFile.getCreatedBy().getUsername() : null)
                .uploadedByUsername(driveFile.getUploadedBy() != null ? driveFile.getUploadedBy().getUsername() : null)
                .build();
    }

    public DriveFolderResponseDTO toDriveFolderResponseDTO(DriveFolder driveFolder) {
        String googleId = driveFolder.getGoogleFolderId();
        if ((googleId == null || googleId.isEmpty()) && driveFolder.getWebViewLink() != null) {
            googleId = GoogleDriveService.extractGoogleIdFromUrl(driveFolder.getWebViewLink());
        }

        return DriveFolderResponseDTO.builder()
                .id(driveFolder.getId())
                .name(driveFolder.getName())
                .googleId(googleId)
                .webViewLink(driveFolder.getWebViewLink())
                .parentFolderId(driveFolder.getParentFolder() != null ? driveFolder.getParentFolder().getId() : null)
                .parentFolderName(driveFolder.getParentFolder() != null ? driveFolder.getParentFolder().getName() : null)
                .createdByUsername(driveFolder.getCreatedBy() != null ? driveFolder.getCreatedBy().getUsername() : null)
                .createdAt(driveFolder.getCreatedAt())
                .updatedAt(driveFolder.getUpdatedAt())
                .build();
    }

    public DriveBookmarkResponseDTO toDriveBookmarkResponseDTO(DriveBookmark bookmark) {
        String googleId = bookmark.getGoogleId();
        if ((googleId == null || googleId.isEmpty()) && bookmark.getUrl() != null) {
            googleId = GoogleDriveService.extractGoogleIdFromUrl(bookmark.getUrl());
        }

        return DriveBookmarkResponseDTO.builder()
                .id(bookmark.getId())
                .name(bookmark.getName())
                .url(bookmark.getUrl())
                .googleId(googleId)
                .type(bookmark.getType())
                .source(bookmark.getSource())
                .createdAt(bookmark.getCreatedAt())
                .updatedAt(bookmark.getUpdatedAt())
                .build();
    }
}
