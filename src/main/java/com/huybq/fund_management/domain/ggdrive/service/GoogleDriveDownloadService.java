package com.huybq.fund_management.domain.ggdrive.service;

import com.google.api.services.drive.Drive;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFile;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFolder;
import com.huybq.fund_management.domain.ggdrive.exception.FileNotFoundException;
import com.huybq.fund_management.domain.ggdrive.exception.FolderNotFoundException;
import com.huybq.fund_management.domain.ggdrive.exception.GoogleDriveException;
import com.huybq.fund_management.domain.ggdrive.repository.DriveFileRepository;
import com.huybq.fund_management.domain.ggdrive.repository.DriveFolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleDriveDownloadService {

    private final DriveFileRepository fileRepository;
    private final DriveFolderRepository folderRepository;
    private final GoogleDriveServiceFactory driveServiceFactory;

    /**
     * Downloads a file from Google Drive using the user's default service
     * account
     */
    @Transactional(readOnly = true)
    public Resource downloadFile(Long fileId, Long userId) throws IOException {
        return downloadFileWithAccount(fileId, userId, null);
    }

    /**
     * Downloads a file from Google Drive using a specific service account
     */
    @Transactional(readOnly = true)
    public Resource downloadFile(Long fileId, Long userId, Long accountId) throws IOException {
        return downloadFileWithAccount(fileId, userId, accountId);
    }

    /**
     * Helper method to download a file with a specific account
     */
    private Resource downloadFileWithAccount(Long fileId, Long userId, Long accountId) throws IOException {
        DriveFile driveFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        try {
            // Get Drive service for the user (default or specific account)
            Drive driveService = accountId != null
                    ? driveServiceFactory.getDriveServiceForAccount(userId, accountId)
                    : driveServiceFactory.getDriveService(userId);

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
     * Downloads a folder as a zip archive using the user's default service
     * account
     */
    @Transactional(readOnly = true)
    public Resource downloadFolderAsZip(Long folderId, Long userId) throws IOException {
        return downloadFolderAsZipWithAccount(folderId, userId, null);
    }

    /**
     * Downloads a folder as a zip archive using a specific service account
     */
    @Transactional(readOnly = true)
    public Resource downloadFolderAsZip(Long folderId, Long userId, Long accountId) throws IOException {
        return downloadFolderAsZipWithAccount(folderId, userId, accountId);
    }

    /**
     * Helper method to download a folder as a zip archive with a specific
     * account
     */
    private Resource downloadFolderAsZipWithAccount(Long folderId, Long userId, Long accountId) throws IOException {
        DriveFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new FolderNotFoundException(folderId));

        // Get Drive service for the user (default or specific account)
        Drive driveService = accountId != null
                ? driveServiceFactory.getDriveServiceForAccount(userId, accountId)
                : driveServiceFactory.getDriveService(userId);

        ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipOutputStream)) {
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
            ZipOutputStream zos,
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
            ZipEntry folderEntry = new ZipEntry(newPathPrefix + "/");
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
            ZipOutputStream zos,
            String pathPrefix) throws IOException {
        try {
            // Create a new entry in the zip file
            String entryName = pathPrefix.isEmpty()
                    ? file.getName()
                    : pathPrefix + "/" + file.getName();
            ZipEntry zipEntry = new ZipEntry(entryName);
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
}
