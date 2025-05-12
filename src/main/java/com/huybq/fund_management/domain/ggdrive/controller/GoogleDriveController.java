package com.huybq.fund_management.domain.ggdrive.controller;

import com.huybq.fund_management.domain.ggdrive.dto.DriveBookmarkResponseDTO;
import com.huybq.fund_management.domain.ggdrive.dto.DriveFileResponseDTO;
import com.huybq.fund_management.domain.ggdrive.dto.DriveFolderResponseDTO;
import com.huybq.fund_management.domain.ggdrive.entity.DriveBookmark;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFile;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFolder;
import com.huybq.fund_management.domain.ggdrive.exception.FileAlreadyExistsException;
import com.huybq.fund_management.domain.ggdrive.exception.FileNotFoundException;
import com.huybq.fund_management.domain.ggdrive.exception.FolderAlreadyExistsException;
import com.huybq.fund_management.domain.ggdrive.exception.FolderNotFoundException;
import com.huybq.fund_management.domain.ggdrive.repository.DriveFileRepository;
import com.huybq.fund_management.domain.ggdrive.repository.DriveFolderRepository;
import com.huybq.fund_management.domain.ggdrive.service.GoogleDriveService;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/drive")
@RequiredArgsConstructor
@Validated
public class GoogleDriveController {

    private final GoogleDriveService driveService;
    private final DriveFileRepository fileRepository;
    private final DriveFolderRepository folderRepository;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/folders/{folderId}/upload")
    public ResponseEntity<DriveFileResponseDTO> uploadFile(
            @PathVariable Long folderId,
            @NotNull @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) throws IOException {
        try {
            return ResponseEntity.ok(driveService.uploadFile(folderId, user.getId(), file));
        } catch (FileAlreadyExistsException e) {
            log.warn("File already exists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal User user) throws IOException {

        driveService.deleteFile(fileId, user);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/folders/{folderId}/contents")
    public ResponseEntity<Map<String, Object>> listFolderContents(
            @PathVariable Long folderId) {
        try {
            Map<String, Object> contents;
            contents = driveService.listFolderContents(folderId);
            return ResponseEntity.ok(contents);
        } catch (Exception e) {
            log.error("Error listing folder contents: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/folders")
    public ResponseEntity<DriveFolderResponseDTO> createFolder(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User user) {
        try {
            DriveFolderResponseDTO folder = driveService.createFolder(
                    request.get("name"),
                    request.get("parentFolderId"),
                    user.getId()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(folder);
        } catch (FolderAlreadyExistsException e) {
            log.warn("Folder already exists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error creating folder: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/folders")
    public ResponseEntity<List<DriveFolderResponseDTO>> getAllFolders() {
        try {
            List<DriveFolderResponseDTO> folders = driveService.getAllFolders();
            return ResponseEntity.ok(folders);
        } catch (Exception e) {
            log.error("Error getting folders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/folders/{folderId}")
    public ResponseEntity<DriveFolderResponseDTO> getFolderById(
            @PathVariable Long folderId) {
        log.info("Getting folder with ID: {}", folderId);
        try {
            DriveFolderResponseDTO folder = driveService.getFolderById(folderId);
            return ResponseEntity.ok(folder);
        } catch (Exception e) {
            log.error("Error getting folder: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/folders/{folderId}")
    public ResponseEntity<DriveFolderResponseDTO> updateFolder(
            @PathVariable Long folderId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User user) {
        log.info("Updating folder with ID: {}", folderId);
        try {
            DriveFolderResponseDTO folder = driveService.updateFolder(
                    folderId,
                    request.get("name"),
                    user
            );
            return ResponseEntity.ok(folder);
        } catch (Exception e) {
            log.error("Error updating folder: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/folders/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @PathVariable Long folderId,
            @AuthenticationPrincipal User user) {
        log.info("Deleting folder with ID: {}", folderId);
        try {
            driveService.deleteFolder(folderId, user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting folder: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/files/{fileId}/move")
    public ResponseEntity<Void> moveFile(
            @PathVariable Long fileId,
            @RequestParam Long targetFolderId,
            @AuthenticationPrincipal User user) {
        log.info("Moving file {} to folder {}", fileId, targetFolderId);
        try {
            driveService.moveFile(fileId, targetFolderId, user.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error moving file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/folders/{folderId}/move")
    public ResponseEntity<Void> moveFolder(
            @PathVariable Long folderId,
            @RequestParam Long targetFolderId,
            @AuthenticationPrincipal User user) {
        log.info("Moving folder {} to folder {}", folderId, targetFolderId);
        try {
            driveService.moveFolder(folderId, targetFolderId, user.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error moving folder: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/files/{fileId}/rename")
    public ResponseEntity<DriveFileResponseDTO> renameFile(
            @PathVariable Long fileId,
            @RequestParam String newName,
            @AuthenticationPrincipal User user) {
        log.info("Renaming file {} to {}", fileId, newName);
        try {
            DriveFileResponseDTO file = driveService.renameFile(fileId, newName, user.getId());
            return ResponseEntity.ok(file);
        } catch (Exception e) {
            log.error("Error renaming file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/folders/{folderId}/rename")
    public ResponseEntity<DriveFolderResponseDTO> renameFolder(
            @PathVariable Long folderId,
            @RequestParam String newName,
            @AuthenticationPrincipal User user) {
        log.info("Renaming folder {} to {}", folderId, newName);
        try {
            DriveFolderResponseDTO folder = driveService.renameFolder(folderId, newName, user.getId());
            return ResponseEntity.ok(folder);
        } catch (Exception e) {
            log.error("Error renaming folder: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/bookmarks")
    public ResponseEntity<DriveBookmarkResponseDTO> createBookmark(
            @NotBlank @RequestParam String name,
            @NotBlank @RequestParam String googleId,
            @NotBlank @RequestParam String type,
            @AuthenticationPrincipal User user) {

        try {
            DriveBookmarkResponseDTO response;
            response = driveService.createBookmark(user.getId(), name, googleId, type);

            // Đảm bảo response luôn có googleId
            if (response.getGoogleId() == null || response.getGoogleId().isEmpty()) {
                log.warn("Failed to get googleId from Google API, using provided googleId instead");

                // Trích xuất googleId từ URL nếu có thể
                String extractedId = GoogleDriveService.extractGoogleIdFromUrl(response.getUrl());
                String idToUse = extractedId != null ? extractedId : googleId;

                // Cập nhật bookmark với googleId
                response = driveService.updateBookmark(response.getId(), response.getName(), user.getId(), idToUse);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log lỗi nhưng tiếp tục với cách tiếp cận fallback
            log.warn("Error creating bookmark with Google Drive API, creating with default URL: {}", e.getMessage());

            // Tạo một fallback bookmark với URL mặc định
            String defaultUrl = type.equalsIgnoreCase("FILE")
                    ? "https://drive.google.com/file/d/" + googleId + "/view"
                    : "https://drive.google.com/drive/folders/" + googleId;

            DriveBookmarkResponseDTO fallbackBookmark = driveService.createExternalBookmark(
                    user.getId(),
                    name,
                    defaultUrl
            );

            // Đảm bảo fallbackBookmark có googleId
            if (fallbackBookmark.getGoogleId() == null || fallbackBookmark.getGoogleId().isEmpty()) {
                fallbackBookmark = driveService.updateBookmark(
                        fallbackBookmark.getId(),
                        fallbackBookmark.getName(),
                        user.getId(),
                        googleId
                );
            }

            return ResponseEntity.ok(fallbackBookmark);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/bookmarks/external")
    public ResponseEntity<DriveBookmarkResponseDTO> createExternalBookmark(
            @NotBlank @RequestParam String name,
            @NotBlank @RequestParam String url,
            @AuthenticationPrincipal User user) {
        log.info("Creating external bookmark '{}' with URL: {}", name, url);

        try {
            DriveBookmarkResponseDTO bookmark = driveService.createExternalBookmark(user.getId(), name, url);

            // Nếu URL là từ Google Drive, trích xuất googleId
            String googleId = GoogleDriveService.extractGoogleIdFromUrl(url);
            if (googleId != null && (bookmark.getGoogleId() == null || bookmark.getGoogleId().isEmpty())) {
                // Cập nhật bookmark với ID đã trích xuất
                bookmark = driveService.updateBookmark(bookmark.getId(), bookmark.getName(), user.getId(), googleId);
            }

            return ResponseEntity.ok(bookmark);
        } catch (Exception e) {
            log.error("Error creating external bookmark: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/bookmarks")
    public ResponseEntity<List<DriveBookmarkResponseDTO>> getUserBookmarks(@AuthenticationPrincipal User user) {
        log.info("Getting bookmarks for user {}", user.getUsername());

        List<DriveBookmarkResponseDTO> bookmarks = driveService.getUserBookmarks(user.getId());

        // Đảm bảo rằng mỗi bookmark có URL chính xác
        bookmarks.forEach(bookmark -> {
            // Nếu là bookmark nội bộ (từ Google Drive), ưu tiên webViewUrl
            if (bookmark.getSource() == DriveBookmark.BookmarkSource.DRIVE) {
                // Sử dụng phương thức helper để lấy URL chính xác
                String webViewUrl = bookmark.getWebViewUrl();
                if (webViewUrl != null && !webViewUrl.isEmpty()) {
                    bookmark.setUrl(webViewUrl);
                }
            }
        });

        return ResponseEntity.ok(bookmarks);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/bookmarks/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(
            @PathVariable Long bookmarkId,
            @AuthenticationPrincipal User user) {
        log.info("Deleting bookmark {}", bookmarkId);
        driveService.deleteBookmark(bookmarkId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/bookmarks/{bookmarkId}")
    public ResponseEntity<DriveBookmarkResponseDTO> updateBookmark(
            @PathVariable Long bookmarkId,
            @RequestParam String name,
            @AuthenticationPrincipal User user) {
        try {
            DriveBookmarkResponseDTO bookmark = driveService.updateBookmark(bookmarkId, name, user.getId());
            return ResponseEntity.ok(bookmark);
        } catch (Exception e) {
            log.error("Error updating bookmark: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/bookmarks/source/{source}")
    public ResponseEntity<List<DriveBookmarkResponseDTO>> getBookmarksBySource(
            @PathVariable String source,
            @AuthenticationPrincipal User user) {
        log.info("Getting bookmarks for user {} with source {}", user.getUsername(), source);
        try {
            List<DriveBookmarkResponseDTO> bookmarks = driveService.getBookmarksBySource(user.getId(), source);
            return ResponseEntity.ok(bookmarks);
        } catch (Exception e) {
            log.error("Error getting bookmarks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long fileId) {
        try {
            // Get file details first to set proper content headers
            DriveFile file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new FileNotFoundException(fileId));

            // Download the file
            Resource resource;
            resource = driveService.downloadFile(fileId);
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
    @GetMapping("/folders/{folderId}/download")
    public ResponseEntity<Resource> downloadFolder(
            @PathVariable Long folderId) {
        try {
            // Get folder details first to set proper content headers
            DriveFolder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new FolderNotFoundException(folderId));

            // Download the folder as zip
            Resource resource;
            resource = driveService.downloadFolderAsZip(folderId);

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

    @ExceptionHandler(FileAlreadyExistsException.class)
    public ResponseEntity<String> handleFileAlreadyExistsException(FileAlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(e.getMessage());
    }

    @ExceptionHandler(FolderAlreadyExistsException.class)
    public ResponseEntity<String> handleFolderAlreadyExistsException(FolderAlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(e.getMessage());
    }
}
