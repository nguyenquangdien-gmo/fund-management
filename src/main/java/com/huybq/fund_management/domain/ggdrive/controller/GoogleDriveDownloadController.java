package com.huybq.fund_management.domain.ggdrive.controller;

import com.huybq.fund_management.domain.ggdrive.entity.DriveFile;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFolder;
import com.huybq.fund_management.domain.ggdrive.exception.FileNotFoundException;
import com.huybq.fund_management.domain.ggdrive.exception.FolderNotFoundException;
import com.huybq.fund_management.domain.ggdrive.repository.DriveFileRepository;
import com.huybq.fund_management.domain.ggdrive.repository.DriveFolderRepository;
import com.huybq.fund_management.domain.ggdrive.service.GoogleDriveDownloadService;
import com.huybq.fund_management.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/drive/download")
@RequiredArgsConstructor
public class GoogleDriveDownloadController {

    private final GoogleDriveDownloadService downloadService;
    private final DriveFileRepository fileRepository;
    private final DriveFolderRepository folderRepository;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/files/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long fileId,
            @RequestParam(name = "accountId", required = false) Long accountId,
            @AuthenticationPrincipal User user) {
        log.info("Downloading file {} using account {}", fileId, accountId != null ? accountId : "default");

        try {
            // Get file details first to set proper content headers
            DriveFile file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new FileNotFoundException(fileId));

            // Download the file
            Resource resource;
            if (accountId != null) {
                resource = downloadService.downloadFile(fileId, user.getId(), accountId);
            } else {
                resource = downloadService.downloadFile(fileId, user.getId());
            }

            String contentDisposition = "attachment; filename=\"" + file.getName() + "\"";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/folders/{folderId}")
    public ResponseEntity<Resource> downloadFolder(
            @PathVariable Long folderId,
            @RequestParam(name = "accountId", required = false) Long accountId,
            @AuthenticationPrincipal User user) {
        log.info("Downloading folder {} as zip using account {}", folderId, accountId != null ? accountId : "default");

        try {
            // Get folder details first to set proper content headers
            DriveFolder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new FolderNotFoundException(folderId));

            // Download the folder as zip
            Resource resource;
            if (accountId != null) {
                resource = downloadService.downloadFolderAsZip(folderId, user.getId(), accountId);
            } else {
                resource = downloadService.downloadFolderAsZip(folderId, user.getId());
            }

            String zipFileName = folder.getName() + ".zip";
            String contentDisposition = "attachment; filename=\"" + zipFileName + "\"";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading folder: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
