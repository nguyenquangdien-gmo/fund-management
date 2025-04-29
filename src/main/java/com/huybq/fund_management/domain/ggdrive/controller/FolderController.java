package com.huybq.fund_management.domain.ggdrive.controller;


import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFolder;
import com.huybq.fund_management.domain.ggdrive.entity.GoogleDriveAccount;
import com.huybq.fund_management.domain.ggdrive.repository.DriveFolderRepository;
import com.huybq.fund_management.domain.ggdrive.repository.GoogleDriveAccountRepository;
import com.huybq.fund_management.domain.ggdrive.service.GoogleDriveService;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/${server.version}/ggdrive/folders")
@RequiredArgsConstructor
public class FolderController {
    private final GoogleDriveService driveService;
    private final DriveFolderRepository folderRepository;
    private final GoogleDriveAccountRepository accountRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<DriveFolder>> getRootFolders() {
        return ResponseEntity.ok(folderRepository.findByParentFolderId(null));
    }

    @PostMapping
    public ResponseEntity<DriveFolder> createFolder(@RequestBody Map<String, String> request,
                                                    @AuthenticationPrincipal User userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getEmail()));
            Long accountId = Long.parseLong(request.get("accountId"));
            String folderName = request.get("name");
            String parentFolderId = request.getOrDefault("parentFolderId", null);

            GoogleDriveAccount account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Google Drive account not found"));

            Drive driveService = getDriveService(account);

            File fileMetadata = new File();
            fileMetadata.setName(folderName);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            if (parentFolderId != null && !parentFolderId.isEmpty()) {
                DriveFolder parentFolder = folderRepository.findById(Long.parseLong(parentFolderId))
                        .orElseThrow(() -> new RuntimeException("Parent folder not found"));
                fileMetadata.setParents(List.of(parentFolder.getGoogleFolderId()));
            } else {
                fileMetadata.setParents(List.of(account.getRootFolderId()));
            }

            File folder = driveService.files().create(fileMetadata)
                    .setFields("id, name, webViewLink")
                    .execute();

            DriveFolder newFolder = new DriveFolder();
            newFolder.setName(folder.getName());
            newFolder.setGoogleFolderId(folder.getId());
            newFolder.setWebViewLink(folder.getWebViewLink());
            newFolder.setGoogleDriveAccount(account);

            if (parentFolderId != null && !parentFolderId.isEmpty()) {
                DriveFolder parentFolder = folderRepository.findById(Long.parseLong(parentFolderId))
                        .orElseThrow(() -> new RuntimeException("Parent folder not found"));
                newFolder.setParentFolder(parentFolder);
            }

            return ResponseEntity.ok(folderRepository.save(newFolder));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // handle IO error with better response
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{folderId}/path")
    public ResponseEntity<List<DriveFolder>> getFolderPath(@PathVariable Long folderId) {
        try {
            DriveFolder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new RuntimeException("Folder not found"));

            List<DriveFolder> path = new ArrayList<>();
            DriveFolder current = folder;

            while (current != null) {
                path.add(0, current);  // Add to beginning of list
                current = current.getParentFolder();
            }

            return ResponseEntity.ok(path);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId,
                                             @AuthenticationPrincipal User userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getEmail()));

            // Kiểm tra quyền Admin
            if ("ADMIN".equals(user.getRole().getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();  // Admin không thể xóa folder
            }

            DriveFolder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new RuntimeException("Folder not found"));

            GoogleDriveAccount account = folder.getGoogleDriveAccount();
            Drive driveService = getDriveService(account);

            driveService.files().delete(folder.getGoogleFolderId()).execute();
            folderRepository.delete(folder);

            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private Drive getDriveService(GoogleDriveAccount account) throws GeneralSecurityException, IOException {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

        GoogleCredentials credentials = GoogleCredentials.create(
                new AccessToken(account.getAccessToken(),
                        java.util.Date.from(account.getTokenExpiryDate().atZone(ZoneId.systemDefault()).toInstant()))
        );

        return new com.google.api.services.drive.Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("Google Drive Management")
                .build();
    }
}
