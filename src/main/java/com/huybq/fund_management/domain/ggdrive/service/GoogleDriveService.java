package com.huybq.fund_management.domain.ggdrive.service;

import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.huybq.fund_management.domain.ggdrive.dto.DriveBookmarkResponseDTO;
import com.huybq.fund_management.domain.ggdrive.dto.DriveFileResponseDTO;
import com.huybq.fund_management.domain.ggdrive.dto.DriveFolderResponseDTO;
import com.huybq.fund_management.domain.ggdrive.entity.DriveBookmark;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFile;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFolder;
import com.huybq.fund_management.domain.ggdrive.exception.*;
import com.huybq.fund_management.domain.ggdrive.mapper.DriveMapper;
import com.huybq.fund_management.domain.ggdrive.repository.DriveBookmarkRepository;
import com.huybq.fund_management.domain.ggdrive.repository.DriveFileRepository;
import com.huybq.fund_management.domain.ggdrive.repository.DriveFolderRepository;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    private final GoogleDriveServiceFactory driveServiceFactory;

    private final DriveMapper driveMapper;

    /**
     * Uploads a file to the specified folder using a specific service account
     */
    @Transactional
    public DriveFileResponseDTO uploadFile(Long folderId, Long userId, MultipartFile file)
            throws IOException {
        DriveFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if file with same name already exists in this folder
        String fileName = file.getOriginalFilename();
        if (fileRepository.existsByNameAndFolder(fileName, folder)) {
            throw new FileAlreadyExistsException(fileName, folderId);
        }
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(folder.getGoogleFolderId()));

        try {
            // Get Drive service for the user (default or specific account)
            Drive driveService = driveServiceFactory.getDriveService();

            File uploadedFile = driveService.files().create(fileMetadata,
                    new InputStreamContent(file.getContentType(), new ByteArrayInputStream(file.getBytes())))
                    .setFields("id, name, mimeType, size, webViewLink, webContentLink, createdTime, modifiedTime")
                    .execute();

            // Set read-only permission for everyone
            Permission permission = new Permission();
            permission.setType("anyone");
            permission.setRole("reader");
            driveService.permissions().create(uploadedFile.getId(), permission).execute();

            // Lưu ý: Đảm bảo googleFileId là ID thực từ Google Drive
            String googleFileId = uploadedFile.getId();

            DriveFile driveFile = new DriveFile();
            driveFile.setName(uploadedFile.getName());
            driveFile.setGoogleFileId(googleFileId); // Sử dụng ID trực tiếp từ Google API
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

            // Tạo DTO với googleFileId lấy trực tiếp từ Google
            DriveFileResponseDTO responseDTO = driveMapper.toDriveFileResponseDTO(savedFile);
            responseDTO.setGoogleFileId(googleFileId);

            return responseDTO;
        } catch (GoogleDriveException e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new GoogleDriveException("Cannot upload file. " + e.getMessage());
        }
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

    /**
     * Deletes a file using a specific service account
     */
    @Transactional
    public void deleteFile(Long fileId, User user) throws IOException {
        DriveFile driveFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Only admin can delete files
        if (!user.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Only admin can delete files");
        }

        try {
            // Get Drive service for the user (default or specific account)
            Drive driveService =driveServiceFactory.getDriveService();

            driveService.files().delete(driveFile.getGoogleFileId()).execute();
            fileRepository.delete(driveFile);
        } catch (GoogleDriveException e) {
            log.error("Failed to delete file: {}", e.getMessage());
            throw new GoogleDriveException("Cannot delete file. " + e.getMessage());
        }
    }

    /**
     * Creates a bookmark using the user's default service account
     */
    @Transactional
    public DriveBookmarkResponseDTO createBookmark(Long userId, String name, String googleId, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DriveBookmark bookmark = new DriveBookmark();
        bookmark.setName(name);
        bookmark.setGoogleId(googleId);
        bookmark.setType(DriveBookmark.BookmarkType.valueOf(type.toUpperCase()));
        bookmark.setSource(DriveBookmark.BookmarkSource.DRIVE);
        bookmark.setUser(user);

        // Cập nhật: Luôn tạo URL chuẩn từ Google ID
        String webViewLink;
        if (type.equalsIgnoreCase("FILE")) {
            webViewLink = "https://drive.google.com/file/d/" + googleId + "/view";
        } else {
            webViewLink = "https://drive.google.com/drive/folders/" + googleId;
        }
        bookmark.setUrl(webViewLink);

        // Thử lấy webViewLink chính xác từ Google API nếu có thể
        try {
            Drive driveService = driveServiceFactory.getDriveService();
            File file = driveService.files()
                    .get(googleId)
                    .setFields("webViewLink")
                    .execute();
            if (file.getWebViewLink() != null) {
                bookmark.setUrl(file.getWebViewLink());
            }
        } catch (Exception e) {
            log.warn("Could not fetch webViewLink for googleId {}: {}. Using default URL.", googleId, e.getMessage());
            // Tiếp tục với URL mặc định đã đặt ở trên
        }

        DriveBookmark savedBookmark = bookmarkRepository.save(bookmark);
        return driveMapper.toDriveBookmarkResponseDTO(savedBookmark);
    }

    /**
     * Creates a bookmark for an external URL
     */
    @Transactional
    public DriveBookmarkResponseDTO createExternalBookmark(Long userId, String name, String url) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DriveBookmark bookmark = new DriveBookmark();
        bookmark.setName(name);
        bookmark.setUrl(url);

        // Kiểm tra xem URL có phải từ Google Drive không
        String googleId = extractGoogleIdFromUrl(url);
        if (googleId != null) {
            bookmark.setGoogleId(googleId);

            // Xác định loại: file hay folder
            if (url.contains("file/d/")) {
                bookmark.setType(DriveBookmark.BookmarkType.FILE);
                bookmark.setSource(DriveBookmark.BookmarkSource.DRIVE);
            } else if (url.contains("folders/")) {
                bookmark.setType(DriveBookmark.BookmarkType.FOLDER);
                bookmark.setSource(DriveBookmark.BookmarkSource.DRIVE);
            } else {
                bookmark.setType(DriveBookmark.BookmarkType.EXTERNAL);
                bookmark.setSource(DriveBookmark.BookmarkSource.EXTERNAL);
            }
        } else {
            bookmark.setType(DriveBookmark.BookmarkType.EXTERNAL);
            bookmark.setSource(DriveBookmark.BookmarkSource.EXTERNAL);
        }

        bookmark.setUser(user);

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

    /**
     * Creates a folder using a specific service account
     */
    @Transactional
    public DriveFolderResponseDTO createFolder(String name, String parentFolderId, Long userId) {

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new GoogleDriveException("User not found"));
            // Check if folder with same name already exists in the parent folder
            if (parentFolderId != null && !parentFolderId.isEmpty()) {
                DriveFolder parentFolder = folderRepository.findById(Long.parseLong(parentFolderId))
                        .orElseThrow(() -> new FolderNotFoundException(Long.parseLong(parentFolderId)));

                if (folderRepository.existsByNameAndParentFolder(name, parentFolder)) {
                    throw new FolderAlreadyExistsException(name, Long.parseLong(parentFolderId));
                }
            } else {
                // Check in the root folder
                if (folderRepository.existsByNameAndParentFolderIsNull(name)) {
                    throw new FolderAlreadyExistsException(name);
                }
            }

            // Get Drive service for the user (default or specific account)
            Drive driveService = driveServiceFactory.getDriveService();

            File fileMetadata = new File();
            fileMetadata.setName(name);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            if (parentFolderId != null && !parentFolderId.isEmpty()) {
                DriveFolder parentFolder = folderRepository.findById(Long.parseLong(parentFolderId))
                        .orElseThrow(() -> new FolderNotFoundException(Long.parseLong(parentFolderId)));
                fileMetadata.setParents(Collections.singletonList(parentFolder.getGoogleFolderId()));
            } else {
                // Get the user's root folder ID (default or specific account)
                String rootFolderId = driveServiceFactory.getRootFolderId();
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

            // Lưu ý: Đảm bảo googleFolderId là ID thực từ Google Drive
            String googleFolderId = folder.getId();

            DriveFolder newFolder = new DriveFolder();
            newFolder.setName(folder.getName());
            newFolder.setGoogleFolderId(googleFolderId); // Sử dụng ID trực tiếp từ Google API
            newFolder.setWebViewLink(folder.getWebViewLink());
            newFolder.setCreatedBy(user);

            if (parentFolderId != null && !parentFolderId.isEmpty()) {
                DriveFolder parentFolder = folderRepository.findById(Long.parseLong(parentFolderId))
                        .orElseThrow(() -> new FolderNotFoundException(Long.parseLong(parentFolderId)));
                newFolder.setParentFolder(parentFolder);
            }

            DriveFolder savedFolder = folderRepository.save(newFolder);

            // Tạo DTO với googleFolderId lấy trực tiếp từ Google
            DriveFolderResponseDTO responseDTO = driveMapper.toDriveFolderResponseDTO(savedFolder);
            responseDTO.setGoogleFolderId(googleFolderId);

            return responseDTO;
        } catch (IOException e) {
            log.error("Failed to create folder in Google Drive: {}", e.getMessage());
            throw new GoogleDriveException("Failed to create folder in Google Drive", e);
        }
    }

    @Transactional(readOnly = true)
    public List<DriveFolderResponseDTO> getAllFolders() {
        List<DriveFolder> folders = folderRepository.findAll();
        return folders.stream()
                .map(driveMapper::toDriveFolderResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DriveFolderResponseDTO getFolderById(Long folderId) {
        DriveFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        return driveMapper.toDriveFolderResponseDTO(folder);
    }

    @Transactional
    public DriveFolderResponseDTO updateFolder(Long folderId, String name, User user) {
        DriveFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        if (!user.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Only admin can delete folders");
        }

        try {
            // Get Drive service for the user
            Drive driveService = driveServiceFactory.getDriveService();

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
            // Get Drive service for the user
            Drive driveService = driveServiceFactory.getDriveService();

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

            // Get Drive service for the user
            Drive driveService = driveServiceFactory.getDriveService();

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

            // Get Drive service for the user
            Drive driveService = driveServiceFactory.getDriveService();

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

            // Get Drive service for the user
            Drive driveService = driveServiceFactory.getDriveService();

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

            // Get Drive service for the user
            Drive driveService = driveServiceFactory.getDriveService();
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
    public DriveBookmarkResponseDTO updateBookmark(Long bookmarkId, String name, Long userId) {
        DriveBookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));

        if (!bookmark.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to update this bookmark");
        }

        bookmark.setName(name);
        DriveBookmark savedBookmark = bookmarkRepository.save(bookmark);
        return driveMapper.toDriveBookmarkResponseDTO(savedBookmark);
    }

    /**
     * Updates a bookmark with a new name and Google ID
     */
    @Transactional
    public DriveBookmarkResponseDTO updateBookmark(Long bookmarkId, String name, Long userId, String googleId) {
        DriveBookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));

        if (!bookmark.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to update this bookmark");
        }

        bookmark.setName(name);
        if (googleId != null && !googleId.isEmpty()) {
            bookmark.setGoogleId(googleId);
        }

        DriveBookmark savedBookmark = bookmarkRepository.save(bookmark);
        return driveMapper.toDriveBookmarkResponseDTO(savedBookmark);
    }

    @Transactional(readOnly = true)
    public List<DriveBookmarkResponseDTO> getBookmarksBySource(Long userId, String source) {
        log.info("Getting bookmarks for user: {} with source: {}", userId, source);
        DriveBookmark.BookmarkSource bookmarkSource = DriveBookmark.BookmarkSource.valueOf(source.toUpperCase());
        List<DriveBookmark> bookmarks = bookmarkRepository.findByUserIdAndSource(userId, bookmarkSource);
        return bookmarks.stream()
                .map(driveMapper::toDriveBookmarkResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to download a file with a specific account
     */
    public Resource downloadFile(Long fileId) {
        DriveFile driveFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        try {
            // Get Drive service for the user (default or specific account)
            Drive driveService = driveServiceFactory.getDriveService();

            // Download the file content
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            driveService.files().get(driveFile.getGoogleFileId())
                    .executeMediaAndDownloadTo(outputStream);

            return new ByteArrayResource(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("Failed to download file: {}", e.getMessage());
            throw new GoogleDriveException("Cannot download file. " + e.getMessage());
        }
    }

    /**
     * Helper method to download a folder as a zip archive with a specific
     * account
     */
    public Resource downloadFolderAsZip(Long folderId) {
        DriveFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new FolderNotFoundException(folderId));

        // Get Drive service for the user (default or specific account)
        Drive driveService = driveServiceFactory.getDriveService();

        ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(zipOutputStream)) {
            // Start with an empty path prefix for the root of the zip archive
            downloadFolderContentsRecursively(driveService, folder, zos, "");
        } catch (Exception e) {
            log.error("Failed to download folder as zip: {}", e.getMessage());
            throw new GoogleDriveException("Cannot download folder. " + e.getMessage());
        }

        return new ByteArrayResource(zipOutputStream.toByteArray());
    }

    /**
     * Recursively download folder contents and add them to the zip archive
     */
    private void downloadFolderContentsRecursively(Drive driveService, DriveFolder folder,
            java.util.zip.ZipOutputStream zos,
            String pathPrefix) throws IOException {
        // First, add all files in the current folder
        List<DriveFile> files = fileRepository.findByFolder(folder);
        for (DriveFile file : files) {
            addFileToZip(driveService, file, zos, pathPrefix);
        }

        // Then, recursively process all subfolders
        List<DriveFolder> subFolders = folderRepository.findByParentFolderId(folder.getId());
        for (DriveFolder subFolder : subFolders) {
            String newPathPrefix = pathPrefix.isEmpty()
                    ? subFolder.getName()
                    : pathPrefix + "/" + subFolder.getName();

            // Create an entry for the folder itself
            java.util.zip.ZipEntry folderEntry = new java.util.zip.ZipEntry(newPathPrefix + "/");
            zos.putNextEntry(folderEntry);
            zos.closeEntry();

            // Process the contents of this subfolder
            downloadFolderContentsRecursively(driveService, subFolder, zos, newPathPrefix);
        }
    }

    /**
     * Add a file to the zip archive
     */
    private void addFileToZip(Drive driveService, DriveFile file,
            java.util.zip.ZipOutputStream zos,
            String pathPrefix) throws IOException {
        try {
            // Create a new entry in the zip file
            String entryName = pathPrefix.isEmpty()
                    ? file.getName()
                    : pathPrefix + "/" + file.getName();
            java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(entryName);
            zos.putNextEntry(zipEntry);

            // Download and write the file contents
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            driveService.files().get(file.getGoogleFileId())
                    .executeMediaAndDownloadTo(outputStream);
            zos.write(outputStream.toByteArray());

            // Close the entry
            zos.closeEntry();
        } catch (Exception e) {
            log.error("Failed to add file to zip: {}", e.getMessage());
            // Continue with other files even if one fails
        }
    }

    @Transactional
    public DriveBookmarkResponseDTO createBookmarkWithAccount(Long userId, Long accountId, String name, String googleId, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DriveBookmark bookmark = new DriveBookmark();
        bookmark.setName(name);
        bookmark.setGoogleId(googleId);
        bookmark.setType(DriveBookmark.BookmarkType.valueOf(type.toUpperCase()));
        bookmark.setSource(DriveBookmark.BookmarkSource.DRIVE);
        bookmark.setUser(user);

        // Set default URL first in case the API call fails
        if (type.equalsIgnoreCase("FILE")) {
            bookmark.setUrl("https://drive.google.com/file/d/" + googleId + "/view");
        } else {
            bookmark.setUrl("https://drive.google.com/drive/folders/" + googleId);
        }

        // Try to get the file/folder details to set the actual URL
        try {
            // Get Drive service for the user with specific account
            Drive driveService = driveServiceFactory.getDriveServiceForAccount(accountId);
            File file = driveService.files()
                    .get(googleId)
                    .setFields("webViewLink")
                    .execute();
            if (file.getWebViewLink() != null) {
                bookmark.setUrl(file.getWebViewLink());
            }
        } catch (Exception e) {
            log.warn("Could not fetch webViewLink for googleId {}: {}. Using default URL.", googleId, e.getMessage());
            // Continue with the default URL set above
        }

        DriveBookmark savedBookmark = bookmarkRepository.save(bookmark);
        return driveMapper.toDriveBookmarkResponseDTO(savedBookmark);
    }

    /**
     * Extracts the Google Drive ID from a webViewLink URL
     *
     * @param webViewLink the Google Drive URL
     * @return the extracted Google Drive ID
     */
    public static String extractGoogleIdFromUrl(String webViewLink) {
        if (webViewLink == null || webViewLink.isEmpty()) {
            return null;
        }

        // For files (pattern: /file/d/{fileId}/view)
        if (webViewLink.contains("/file/d/")) {
            int startIndex = webViewLink.indexOf("/file/d/") + 8;
            int endIndex = webViewLink.indexOf("/view");
            if (endIndex > startIndex) {
                return webViewLink.substring(startIndex, endIndex);
            }
        }

        // For folders (pattern: /folders/{folderId})
        if (webViewLink.contains("/folders/")) {
            int startIndex = webViewLink.indexOf("/folders/") + 9;
            int endIndex = webViewLink.indexOf("?", startIndex);
            if (endIndex == -1) {
                // No query parameters
                return webViewLink.substring(startIndex);
            } else {
                return webViewLink.substring(startIndex, endIndex);
            }
        }

        return null;
    }
}
