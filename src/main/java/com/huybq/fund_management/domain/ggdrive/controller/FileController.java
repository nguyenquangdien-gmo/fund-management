package com.huybq.fund_management.domain.ggdrive.controller;

import com.huybq.fund_management.domain.ggdrive.entity.DriveFile;
import com.huybq.fund_management.domain.ggdrive.service.GoogleDriveService;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

@RestController
@RequestMapping("/api/${server.version}/ggdrive/files")
@RequiredArgsConstructor
public class FileController {

    private final GoogleDriveService driveService;
    private final UserRepository userRepository;

    @GetMapping("/{fileId}")
    public ResponseEntity<DriveFile> getFileDetails(@PathVariable Long fileId) {
        try {
            DriveFile file = driveService.getFileById(fileId);
            return ResponseEntity.ok(file);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            DriveFile file = driveService.getFileById(fileId);
            byte[] fileContent = driveService.downloadFile(fileId);

            ByteArrayResource resource = new ByteArrayResource(fileContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .contentLength(fileContent.length)
                    .contentType(MediaType.parseMediaType(file.getMimeType()))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId,
                                           @AuthenticationPrincipal User userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getEmail()).orElseThrow(()-> new ResourceNotFoundException("User not found with email: " + userDetails.getEmail()));
            driveService.deleteFile(fileId, user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<DriveFile> uploadFile(
            @RequestParam("driveAccountId") Long driveAccountId,
            @RequestParam("folderId") Long folderId,
            @RequestParam("userId") Long userId,
            @RequestParam("file") MultipartFile file) throws IOException, GeneralSecurityException {

        DriveFile uploadedFile = driveService.uploadFile(driveAccountId, folderId, userId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedFile);
    }

    // Endpoint để liệt kê nội dung folder
    @GetMapping("/folder/{folderId}/contents")
    public ResponseEntity<Map<String, Object>> listFolderContents(@PathVariable Long folderId) throws IOException, GeneralSecurityException {
        Map<String, Object> folderContents = driveService.listFolderContents(folderId);
        return ResponseEntity.ok(folderContents);
    }
}
