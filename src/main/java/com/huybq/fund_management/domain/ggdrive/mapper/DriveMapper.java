package com.huybq.fund_management.domain.ggdrive.mapper;

import com.huybq.fund_management.domain.ggdrive.dto.DriveBookmarkResponseDTO;
import com.huybq.fund_management.domain.ggdrive.dto.DriveFileResponseDTO;
import com.huybq.fund_management.domain.ggdrive.dto.DriveFolderResponseDTO;
import com.huybq.fund_management.domain.ggdrive.entity.DriveBookmark;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFile;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DriveMapper {

    public DriveFileResponseDTO toDriveFileResponseDTO(DriveFile file) {
        if (file == null) {
            return null;
        }

        return DriveFileResponseDTO.builder()
                .id(file.getId())
                .name(file.getName())
                .mimeType(file.getMimeType())
                .size(file.getSize())
                .webViewLink(file.getWebViewLink())
                .webContentLink(file.getWebContentLink())
                .createdTime(file.getCreatedTime())
                .modifiedTime(file.getModifiedTime())
                .folderId(file.getFolder() != null ? file.getFolder().getId() : null)
                .folderName(file.getFolder() != null ? file.getFolder().getName() : null)
                .createdByUsername(file.getCreatedBy() != null ? file.getCreatedBy().getUsername() : null)
                .build();
    }

    public DriveFolderResponseDTO toDriveFolderResponseDTO(DriveFolder folder) {
        if (folder == null) {
            return null;
        }

        return DriveFolderResponseDTO.builder()
                .id(folder.getId())
                .name(folder.getName())
                .webViewLink(folder.getWebViewLink())
                .createdTime(folder.getCreatedTime())
                .modifiedTime(folder.getModifiedTime())
                .parentFolderId(folder.getParentFolder() != null ? folder.getParentFolder().getId() : null)
                .parentFolderName(folder.getParentFolder() != null ? folder.getParentFolder().getName() : null)
                .createdByUsername(folder.getCreatedBy() != null ? folder.getCreatedBy().getUsername() : null)
                .build();
    }

    public DriveBookmarkResponseDTO toDriveBookmarkResponseDTO(DriveBookmark bookmark) {
        if (bookmark == null) {
            return null;
        }

        return DriveBookmarkResponseDTO.builder()
                .id(bookmark.getId())
                .name(bookmark.getName())
                .url(bookmark.getUrl())
                .type(bookmark.getType())
                .googleId(bookmark.getGoogleId())
                .category(bookmark.getCategory())
                .createdAt(bookmark.getCreatedAt())
                .updatedAt(bookmark.getUpdatedAt())
                .folderId(bookmark.getFolder() != null ? bookmark.getFolder().getId() : null)
                .folderName(bookmark.getFolder() != null ? bookmark.getFolder().getName() : null)
                .createdByUsername(bookmark.getUser() != null ? bookmark.getUser().getUsername() : null)
                .build();
    }
}
