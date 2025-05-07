package com.huybq.fund_management.domain.ggdrive.controller;

import com.huybq.fund_management.domain.ggdrive.dto.DriveBookmarkResponseDTO;
import com.huybq.fund_management.domain.ggdrive.dto.DriveFileResponseDTO;
import com.huybq.fund_management.domain.ggdrive.dto.DriveFolderResponseDTO;
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
    private final UserRepository userRepository;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/folders/{folderId}/upload")
    public ResponseEntity<DriveFileResponseDTO> uploadFile(
            @PathVariable Long folderId,
            @NotNull @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) throws IOException {
        log.info("Uploading file {} to folder {}", file.getOriginalFilename(), folderId);
        return ResponseEntity.ok(driveService.uploadFile(folderId, user.getId(), file));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal User user) throws IOException {
        log.info("Deleting file {}", fileId);
        driveService.deleteFile(fileId, user);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/folders/{folderId}/contents")
    public ResponseEntity<Map<String, Object>> listFolderContents(
            @PathVariable Long folderId) {
        log.info("Listing contents of folder {}", folderId);
        return ResponseEntity.ok(driveService.listFolderContents(folderId));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/bookmarks")
    public ResponseEntity<DriveBookmarkResponseDTO> createBookmark(
            @NotBlank @RequestParam String name,
            @NotBlank @RequestParam String googleId,
            @NotBlank @RequestParam String type,
            @AuthenticationPrincipal User user) {
        log.info("Creating bookmark {} for {}", name, googleId);
        return ResponseEntity.ok(driveService.createBookmark(user.getId(), name, googleId, type));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/bookmarks")
    public ResponseEntity<List<DriveBookmarkResponseDTO>> getUserBookmarks(@AuthenticationPrincipal User user) {
        log.info("Getting bookmarks for user {}", user.getUsername());
        return ResponseEntity.ok(driveService.getUserBookmarks(user.getId()));
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
    @PostMapping("/folders")
    public ResponseEntity<DriveFolderResponseDTO> createFolder(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User user) {
        log.info("Creating new folder: {}", request.get("name"));
        try {
            DriveFolderResponseDTO folder = driveService.createFolder(
                    request.get("name"),
                    request.get("parentFolderId"),
                    user.getId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(folder);
        } catch (Exception e) {
            log.error("Error creating folder: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/folders")
    public ResponseEntity<List<DriveFolderResponseDTO>> getAllFolders(@AuthenticationPrincipal User user) {
        log.info("Getting all folders for user: {}", user.getUsername());
        try {
            List<DriveFolderResponseDTO> folders = driveService.getAllFolders(user.getId());
            return ResponseEntity.ok(folders);
        } catch (Exception e) {
            log.error("Error getting folders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/folders/{folderId}")
    public ResponseEntity<DriveFolderResponseDTO> getFolderById(
            @PathVariable Long folderId,
            @AuthenticationPrincipal User user) {
        log.info("Getting folder with ID: {}", folderId);
        try {
            DriveFolderResponseDTO folder = driveService.getFolderById(folderId, user.getId());
            return ResponseEntity.ok(folder);
        } catch (Exception e) {
            log.error("Error getting folder: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
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
                    user.getId()
            );
            return ResponseEntity.ok(folder);
        } catch (Exception e) {
            log.error("Error updating folder: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
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

    @PreAuthorize("hasRole('ADMIN')")
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

    @PreAuthorize("hasRole('ADMIN')")
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

    @PreAuthorize("hasRole('ADMIN')")
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

    @PreAuthorize("hasRole('ADMIN')")
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
    @PutMapping("/bookmarks/{bookmarkId}")
    public ResponseEntity<DriveBookmarkResponseDTO> updateBookmark(
            @PathVariable Long bookmarkId,
            @RequestParam String name,
            @RequestParam String category,
            @AuthenticationPrincipal User user) {
        log.info("Updating bookmark {} with name {} and category {}", bookmarkId, name, category);
        try {
            DriveBookmarkResponseDTO bookmark = driveService.updateBookmark(bookmarkId, name, category, user.getId());
            return ResponseEntity.ok(bookmark);
        } catch (Exception e) {
            log.error("Error updating bookmark: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/bookmarks/category/{category}")
    public ResponseEntity<List<DriveBookmarkResponseDTO>> getBookmarksByCategory(
            @PathVariable String category,
            @AuthenticationPrincipal User user) {
        log.info("Getting bookmarks for user {} in category {}", user.getUsername(), category);
        try {
            List<DriveBookmarkResponseDTO> bookmarks = driveService.getBookmarksByCategory(user.getId(), category);
            return ResponseEntity.ok(bookmarks);
        } catch (Exception e) {
            log.error("Error getting bookmarks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
