package com.huybq.fund_management.domain.ggdrive.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.huybq.fund_management.domain.ggdrive.entity.UserGoogleServiceAccount;
import com.huybq.fund_management.domain.ggdrive.exception.GoogleDriveException;
import com.huybq.fund_management.domain.ggdrive.repository.UserGoogleServiceAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleDriveServiceFactory {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final UserGoogleServiceAccountRepository serviceAccountRepository;

    // Single cached instance of the Drive service for the default service account
    private Drive defaultDriveService;

    /**
     * Gets the Drive service using the default active service account
     * This method is used by all members to upload files
     */
    public Drive getDriveService() {
        // Check cache first
        if (defaultDriveService != null) {
            return defaultDriveService;
        }

        UserGoogleServiceAccount account = serviceAccountRepository.findByIsDefaultTrueAndIsActiveTrue()
                .orElseThrow(() -> new GoogleDriveException("No active default Google Service Account configured"));

        try {
            Drive driveService = createDriveService(
                    account.getCredentialsFilePath(),
                    account.getApplicationName()
            );

            // Update connection status
            updateConnectionStatus(account, true, null);

            // Cache the service
            defaultDriveService = driveService;

            return driveService;
        } catch (Exception e) {
            log.error("Failed to create Drive service: {}", e.getMessage());
            updateConnectionStatus(account, false, e.getMessage());
            throw new GoogleDriveException("Failed to connect to Google Drive: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a Drive service for a specific service account
     * This may be used for administrative purposes
     */
    public Drive getDriveServiceForAccount(Long accountId) {
        UserGoogleServiceAccount account = serviceAccountRepository.findById(accountId)
                .orElseThrow(() -> new GoogleDriveException("Service account not found"));

        if (!account.getIsActive()) {
            throw new GoogleDriveException("Service account is not active");
        }

        try {
            Drive driveService = createDriveService(
                    account.getCredentialsFilePath(),
                    account.getApplicationName()
            );

            updateConnectionStatus(account, true, null);
            return driveService;
        } catch (Exception e) {
            log.error("Failed to create Drive service for account {}: {}", accountId, e.getMessage());
            updateConnectionStatus(account, false, e.getMessage());
            throw new GoogleDriveException("Failed to connect to Google Drive: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the root folder ID for the default service account
     */
    public String getRootFolderId() {
        UserGoogleServiceAccount account = serviceAccountRepository.findByIsDefaultTrueAndIsActiveTrue()
                .orElseThrow(() -> new GoogleDriveException("No active default Google Service Account configured"));

        return account.getRootFolderId();
    }

    /**
     * Creates a Drive service using the provided credentials file and application name
     */
    private Drive createDriveService(String credentialsFilePath, String applicationName) {
        try {
            Path path = Paths.get(credentialsFilePath);
            if (!Files.exists(path)) {
                throw new GoogleDriveException("Credentials file does not exist: " + credentialsFilePath);
            }

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new FileInputStream(credentialsFilePath))
                    .createScoped(Collections.singleton(DriveScopes.DRIVE));

            return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                    .setApplicationName(applicationName)
                    .build();
        } catch (IOException | GeneralSecurityException e) {
            throw new GoogleDriveException("Failed to create Drive service", e);
        }
    }

    /**
     * Update service account connection status
     */
    private void updateConnectionStatus(UserGoogleServiceAccount account, boolean isConnected, String errorMessage) {
        account.setConnectionStatus(isConnected ?
                UserGoogleServiceAccount.ConnectionStatus.CONNECTED :
                UserGoogleServiceAccount.ConnectionStatus.FAILED);
        account.setLastConnectionCheck(LocalDateTime.now());
        account.setConnectionError(errorMessage);
        serviceAccountRepository.save(account);
    }

    /**
     * Invalidates the cached Drive service
     */
    public void invalidateCache() {
        defaultDriveService = null;
    }

    public static class TestDriveBuilder {

        private final String credentialsFilePath;
        private final String applicationName;

        public TestDriveBuilder(String credentialsFilePath, String applicationName) {
            this.credentialsFilePath = credentialsFilePath;
            this.applicationName = applicationName;
        }

        public Drive build() throws IOException, GeneralSecurityException {
            Path path = Paths.get(credentialsFilePath);
            if (!Files.exists(path)) {
                throw new GoogleDriveException("Credentials file does not exist: " + credentialsFilePath);
            }

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new FileInputStream(credentialsFilePath))
                    .createScoped(Collections.singleton(DriveScopes.DRIVE));

            return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                    .setApplicationName(applicationName)
                    .build();
        }
    }
}