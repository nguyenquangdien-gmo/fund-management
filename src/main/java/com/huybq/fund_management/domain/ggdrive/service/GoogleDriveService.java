package com.huybq.fund_management.domain.ggdrive.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.huybq.fund_management.domain.ggdrive.dto.DriveBookmarkResponseDTO;
import com.huybq.fund_management.domain.ggdrive.dto.DriveFileResponseDTO;
import com.huybq.fund_management.domain.ggdrive.dto.DriveFolderResponseDTO;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFile;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFolder;
import com.huybq.fund_management.domain.ggdrive.entity.DriveBookmark;
import com.huybq.fund_management.domain.ggdrive.exception.FileNotFoundException;
import com.huybq.fund_management.domain.ggdrive.exception.FolderNotFoundException;
import com.huybq.fund_management.domain.ggdrive.exception.GoogleDriveException;
import com.huybq.fund_management.domain.ggdrive.exception.PermissionDeniedException;
import com.huybq.fund_management.domain.ggdrive.mapper.DriveMapper;
import com.huybq.fund_management.domain.ggdrive.repository.DriveFileRepository;
import com.huybq.fund_management.domain.ggdrive.repository.DriveFolderRepository;
import com.huybq.fund_management.domain.ggdrive.repository.DriveBookmarkRepository;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleDriveService {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final DriveFolderRepository folderRepository;

    private final DriveFileRepository fileRepository;

    private final DriveBookmarkRepository bookmarkRepository;

    private final UserRepository userRepository;

    private final Drive driveService;

    private final DriveMapper driveMapper;

    @Value("${google.drive.root-folder-id}")
    private String rootFolderId;

    @Transactional
    public DriveFileResponseDTO uploadFile(Long folderId, Long userId, MultipartFile file)
            throws IOException {
        DriveFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        File fileMetadata = new File();
        fileMetadata.setName(file.getOriginalFilename());
        fileMetadata.setParents(Collections.singletonList(folder.getGoogleFolderId()));

        File uploadedFile = driveService.files().create(fileMetadata,
                new InputStreamContent(file.getContentType(), new ByteArrayInputStream(file.getBytes())))
                .setFields("id, name, mimeType, size, webViewLink, webContentLink, createdTime, modifiedTime")
                .execute();

        // Set read-only permission for everyone
        Permission permission = new Permission();
        permission.setType("anyone");
        permission.setRole("reader");
        driveService.permissions().create(uploadedFile.getId(), permission).execute();

        DriveFile driveFile = new DriveFile();
        driveFile.setName(uploadedFile.getName());
        driveFile.setGoogleFileId(uploadedFile.getId());
        driveFile.setMimeType(uploadedFile.getMimeType());
        driveFile.setSize(uploadedFile.getSize());
        driveFile.setWebViewLink(uploadedFile.getWebViewLink());
        driveFile.setWebContentLink(uploadedFile.getWebContentLink());
        driveFile.setCreatedTime(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(uploadedFile.getCreatedTime().getValue()), ZoneId.systemDefault()));
        driveFile.setModifiedTime(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(uploadedFile.getModifiedTime().getValue()), ZoneId.systemDefault()));
        driveFile.setFolder(folder);
        driveFile.setUploadedBy(user);
        driveFile.setCreatedBy(user);

        DriveFile savedFile = fileRepository.save(driveFile);
        return driveMapper.toDriveFileResponseDTO(savedFile);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listFolderContents(Long folderId) {
        DriveFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("currentFolder", driveMapper.toDriveFolderResponseDTO(folder));
        result.put("subFolders", folderRepository.findByParentFolderId(folder.getId())
                .stream()
                .map(driveMapper::toDriveFolderResponseDTO)
                .collect(Collectors.toList()));
        result.put("files", fileRepository.findByFolder(folder)
                .stream()
                .map(driveMapper::toDriveFileResponseDTO)
                .collect(Collectors.toList()));

        return result;
    }

    @Transactional
    public void deleteFile(Long fileId, User user) throws IOException {
        DriveFile driveFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Only admin can delete files
        if (!user.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Only admin can delete files");
        }

        driveService.files().delete(driveFile.getGoogleFileId()).execute();
        fileRepository.delete(driveFile);
    }

    @Transactional
    public DriveBookmarkResponseDTO createBookmark(Long userId, String name, String googleId, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DriveBookmark bookmark = new DriveBookmark();
        bookmark.setName(name);
        bookmark.setGoogleId(googleId);
        bookmark.setType(type);
        bookmark.setUser(user);

        // Get the file/folder details to set the URL
        try {
            File file = driveService.files().get(googleId)
                    .setFields("webViewLink")
                    .execute();
            bookmark.setUrl(file.getWebViewLink());
        } catch (IOException e) {
            throw new RuntimeException("Failed to get file/folder details", e);
        }

        DriveBookmark savedBookmark = bookmarkRepository.save(bookmark);
        return driveMapper.toDriveBookmarkResponseDTO(savedBookmark);
    }

    @Transactional(readOnly = true)
    public List<DriveBookmarkResponseDTO> getUserBookmarks(Long userId) {
        List<DriveBookmark> bookmarks = bookmarkRepository.findByUserId(userId);
        return bookmarks.stream()
                .map(driveMapper::toDriveBookmarkResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteBookmark(Long bookmarkId, Long userId) {
        DriveBookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));

        if (!bookmark.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own bookmarks");
        }

        bookmarkRepository.delete(bookmark);
    }

    @Transactional
    public DriveFolderResponseDTO createFolder(String name, String parentFolderId, Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new GoogleDriveException("User not found"));

            File fileMetadata = new File();
            fileMetadata.setName(name);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            if (parentFolderId != null && !parentFolderId.isEmpty()) {
                DriveFolder parentFolder = folderRepository.findById(Long.parseLong(parentFolderId))
                        .orElseThrow(() -> new FolderNotFoundException(Long.parseLong(parentFolderId)));
                fileMetadata.setParents(Collections.singletonList(parentFolder.getGoogleFolderId()));
            } else {
                fileMetadata.setParents(Collections.singletonList(rootFolderId));
            }

            File folder = driveService.files().create(fileMetadata)
                    .setFields("id, name, webViewLink")
                    .execute();

            // Set read-only permission for everyone
            Permission permission = new Permission();
            permission.setType("anyone");
            permission.setRole("reader");
            driveService.permissions().create(folder.getId(), permission)
                    .setSendNotificationEmail(false)
                    .execute();

            DriveFolder newFolder = new DriveFolder();
            newFolder.setName(folder.getName());
            newFolder.setGoogleFolderId(folder.getId());
            newFolder.setWebViewLink(folder.getWebViewLink());
            newFolder.setCreatedBy(user);

            if (parentFolderId != null && !parentFolderId.isEmpty()) {
                DriveFolder parentFolder = folderRepository.findById(Long.parseLong(parentFolderId))
                        .orElseThrow(() -> new FolderNotFoundException(Long.parseLong(parentFolderId)));
                newFolder.setParentFolder(parentFolder);
            }

            DriveFolder savedFolder = folderRepository.save(newFolder);
            return driveMapper.toDriveFolderResponseDTO(savedFolder);
        } catch (IOException e) {
            log.error("Failed to create folder in Google Drive: {}", e.getMessage());
            throw new GoogleDriveException("Failed to create folder in Google Drive", e);
        }
    }

    @Transactional(readOnly = true)
    public List<DriveFolderResponseDTO> getAllFolders(Long userId) {
        List<DriveFolder> folders = folderRepository.findByCreatedBy_Id(userId);
        return folders.stream()
                .map(driveMapper::toDriveFolderResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DriveFolderResponseDTO getFolderById(Long folderId, Long userId) {
        DriveFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        if (!folder.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to access this folder");
        }

        return driveMapper.toDriveFolderResponseDTO(folder);
    }

    @Transactional
    public DriveFolderResponseDTO updateFolder(Long folderId, String name, Long userId) {
        DriveFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        if (!folder.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to update this folder");
        }

        try {
            File fileMetadata = new File();
            fileMetadata.setName(name);

            File updatedFolder = driveService.files().update(folder.getGoogleFolderId(), fileMetadata)
                    .setFields("id, name, webViewLink")
                    .execute();

            folder.setName(updatedFolder.getName());
            folder.setWebViewLink(updatedFolder.getWebViewLink());

            DriveFolder savedFolder = folderRepository.save(folder);
            return driveMapper.toDriveFolderResponseDTO(savedFolder);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update folder in Google Drive", e);
        }
    }

    @Transactional
    public void deleteFolder(Long folderId, User user) {
        DriveFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        // Only admin can delete folders
        if (!user.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Only admin can delete folders");
        }

        try {
            // Delete all files in the folder
            List<DriveFile> files = fileRepository.findByFolder(folder);
            for (DriveFile file : files) {
                driveService.files().delete(file.getGoogleFileId()).execute();
                fileRepository.delete(file);
            }

            // Delete all subfolders recursively
            List<DriveFolder> subFolders = folderRepository.findByParentFolderId(folderId);
            for (DriveFolder subFolder : subFolders) {
                deleteFolder(subFolder.getId(), user);
            }

            // Delete the folder itself
            driveService.files().delete(folder.getGoogleFolderId()).execute();
            folderRepository.delete(folder);
        } catch (IOException e) {
            log.error("Failed to delete folder from Google Drive: {}", e.getMessage());
            throw new GoogleDriveException("Failed to delete folder from Google Drive", e);
        }
    }

    @Transactional
    public void moveFile(Long fileId, Long targetFolderId, Long userId) {
        log.info("Moving file: {} to folder: {}", fileId, targetFolderId);
        try {
            DriveFile file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new FileNotFoundException(fileId));

            DriveFolder targetFolder = folderRepository.findById(targetFolderId)
                    .orElseThrow(() -> new FolderNotFoundException(targetFolderId));

            if (!file.getCreatedBy().getId().equals(userId)) {
                throw new PermissionDeniedException("You don't have permission to move this file");
            }

            // Update in Google Drive
            File fileMetadata = new File();
            fileMetadata.setParents(Collections.singletonList(targetFolder.getGoogleFolderId()));
            driveService.files().update(file.getGoogleFileId(), fileMetadata)
                    .setFields("id")
                    .execute();

            // Update in database
            file.setFolder(targetFolder);
            fileRepository.save(file);
        } catch (IOException e) {
            log.error("Failed to move file: {}", e.getMessage());
            throw new GoogleDriveException("Failed to move file", e);
        }
    }

    @Transactional
    public void moveFolder(Long folderId, Long targetFolderId, Long userId) {
        log.info("Moving folder: {} to folder: {}", folderId, targetFolderId);
        try {
            DriveFolder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new FolderNotFoundException(folderId));

            DriveFolder targetFolder = folderRepository.findById(targetFolderId)
                    .orElseThrow(() -> new FolderNotFoundException(targetFolderId));

            // Check if target folder is a subfolder of the source folder
            if (isSubfolder(targetFolder, folder)) {
                throw new GoogleDriveException("Cannot move a folder into its own subfolder");
            }

            // Check permissions
            if (!folder.getCreatedBy().getId().equals(userId)) {
                throw new PermissionDeniedException("You don't have permission to move this folder");
            }

            if (!targetFolder.getCreatedBy().getId().equals(userId)) {
                throw new PermissionDeniedException("You don't have permission to move to the target folder");
            }

            // Update in Google Drive
            File fileMetadata = new File();
            fileMetadata.setParents(Collections.singletonList(targetFolder.getGoogleFolderId()));
            File updatedFolder = driveService.files().update(folder.getGoogleFolderId(), fileMetadata)
                    .setFields("id, webViewLink")
                    .execute();

            // Update in database
            folder.setParentFolder(targetFolder);
            folder.setWebViewLink(updatedFolder.getWebViewLink());
            folderRepository.save(folder);
        } catch (IOException e) {
            log.error("Failed to move folder: {}", e.getMessage());
            throw new GoogleDriveException("Failed to move folder", e);
        }
    }

    private boolean isSubfolder(DriveFolder folder, DriveFolder potentialParent) {
        if (folder == null || potentialParent == null) {
            return false;
        }
        if (folder.getParentFolder() == null) {
            return false;
        }
        if (folder.getParentFolder().getId().equals(potentialParent.getId())) {
            return true;
        }
        return isSubfolder(folder.getParentFolder(), potentialParent);
    }

    @Transactional
    public DriveFileResponseDTO renameFile(Long fileId, String newName, Long userId) {
        log.info("Renaming file: {} to: {}", fileId, newName);
        try {
            DriveFile file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new FileNotFoundException(fileId));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Only admin can rename files
            if (!user.getRole().getName().equals("ADMIN")) {
                throw new PermissionDeniedException("Only admin can rename files");
            }

            File fileMetadata = new File();
            fileMetadata.setName(newName);
            File updatedFile = driveService.files().update(file.getGoogleFileId(), fileMetadata)
                    .setFields("id, name")
                    .execute();

            file.setName(updatedFile.getName());
            DriveFile savedFile = fileRepository.save(file);
            return driveMapper.toDriveFileResponseDTO(savedFile);
        } catch (IOException e) {
            log.error("Failed to rename file: {}", e.getMessage());
            throw new GoogleDriveException("Failed to rename file", e);
        }
    }

    @Transactional
    public DriveFolderResponseDTO renameFolder(Long folderId, String newName, Long userId) {
        log.info("Renaming folder: {} to: {}", folderId, newName);
        try {
            DriveFolder folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new FolderNotFoundException(folderId));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Only admin can rename folders
            if (!user.getRole().getName().equals("ADMIN")) {
                throw new PermissionDeniedException("Only admin can rename folders");
            }

            File fileMetadata = new File();
            fileMetadata.setName(newName);
            File updatedFolder = driveService.files().update(folder.getGoogleFolderId(), fileMetadata)
                    .setFields("id, name")
                    .execute();

            folder.setName(updatedFolder.getName());
            folder.setWebViewLink(updatedFolder.getWebViewLink());
            DriveFolder savedFolder = folderRepository.save(folder);
            return driveMapper.toDriveFolderResponseDTO(savedFolder);
        } catch (IOException e) {
            log.error("Failed to rename folder: {}", e.getMessage());
            throw new GoogleDriveException("Failed to rename folder", e);
        }
    }

    @Transactional
    public DriveBookmarkResponseDTO updateBookmark(Long bookmarkId, String name, String category, Long userId) {
        log.info("Updating bookmark: {} with name: {} and category: {}", bookmarkId, name, category);
        DriveBookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new GoogleDriveException("Bookmark not found"));

        if (!bookmark.getUser().getId().equals(userId)) {
            throw new PermissionDeniedException("You don't have permission to update this bookmark");
        }

        bookmark.setName(name);
        bookmark.setCategory(category);
        DriveBookmark savedBookmark = bookmarkRepository.save(bookmark);
        return driveMapper.toDriveBookmarkResponseDTO(savedBookmark);
    }

    @Transactional(readOnly = true)
    public List<DriveBookmarkResponseDTO> getBookmarksByCategory(Long userId, String category) {
        log.info("Getting bookmarks for user: {} in category: {}", userId, category);
        List<DriveBookmark> bookmarks = bookmarkRepository.findByUserIdAndCategory(userId, category);
        return bookmarks.stream()
                .map(driveMapper::toDriveBookmarkResponseDTO)
                .collect(Collectors.toList());
    }

}
