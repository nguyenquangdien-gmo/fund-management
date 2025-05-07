package com.huybq.fund_management.domain.ggdrive.service;

import com.huybq.fund_management.domain.ggdrive.exception.GoogleDriveException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class GoogleServiceAccountStorageService {

    @Value("${google.service.account.storage.location:${user.home}/fund-management/service-accounts}")
    private String storageLocation;

    public void init() {
        try {
            Path storagePath = Paths.get(storageLocation);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }
        } catch (IOException e) {
            throw new GoogleDriveException("Could not initialize storage location", e);
        }
    }

    /**
     * Stores a service account credentials file
     *
     * @param file The credentials file to store
     * @param userId The user ID
     * @return The path to the stored file
     */
    public String storeCredentialsFile(MultipartFile file, Long userId) {
        try {
            // Validate file is JSON
            if (file.getContentType() == null || !file.getContentType().equals("application/json")) {
                throw new GoogleDriveException("Only JSON files are allowed for service account credentials");
            }

            // Create unique filename (userId_UUID.json)
            String filename = userId + "_" + UUID.randomUUID().toString() + ".json";
            Path targetPath = Paths.get(storageLocation).resolve(filename);

            // Copy the file to the target location
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return targetPath.toString();
        } catch (IOException e) {
            throw new GoogleDriveException("Failed to store credentials file", e);
        }
    }

    /**
     * Stores a service account credentials file with account name included in
     * filename
     *
     * @param file The credentials file to store
     * @param userId The user ID
     * @param accountName The account name for better identification
     * @return The path to the stored file
     */
    public String storeCredentialsFile(MultipartFile file, Long userId, String accountName) {
        try {
            // Validate file is JSON
            if (file.getContentType() == null || !file.getContentType().equals("application/json")) {
                throw new GoogleDriveException("Only JSON files are allowed for service account credentials");
            }

            // Sanitize account name for file system
            String sanitizedName = accountName.replaceAll("[^a-zA-Z0-9_-]", "_");

            // Create unique filename with account name for better identification
            // Format: userId_accountName_UUID.json
            String filename = userId + "_" + sanitizedName + "_" + UUID.randomUUID().toString() + ".json";
            Path targetPath = Paths.get(storageLocation).resolve(filename);

            // Create user directory if it doesn't exist
            Path userDir = Paths.get(storageLocation).resolve(userId.toString());
            if (!Files.exists(userDir)) {
                Files.createDirectories(userDir);
            }

            // Use user-specific directory for better organization
            Path finalPath = userDir.resolve(filename);

            // Copy the file to the target location
            Files.copy(file.getInputStream(), finalPath, StandardCopyOption.REPLACE_EXISTING);

            return finalPath.toString();
        } catch (IOException e) {
            throw new GoogleDriveException("Failed to store credentials file", e);
        }
    }

    public void deleteCredentialsFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new GoogleDriveException("Failed to delete credentials file", e);
        }
    }
}
