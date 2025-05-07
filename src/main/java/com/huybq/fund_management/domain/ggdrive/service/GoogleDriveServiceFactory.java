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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleDriveServiceFactory {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final UserGoogleServiceAccountRepository serviceAccountRepository;

    // Cache for Drive services to avoid recreating them for each request
    private final Map<Long, Drive> driveServicesCache = new HashMap<>();

    /**
     * Gets a Drive service for a user based on their default active service
     * account Throws an exception if no service account is configured for the
     * user
     */
    public Drive getDriveService(Long userId) {
        // Check cache first
        if (driveServicesCache.containsKey(userId)) {
            return driveServicesCache.get(userId);
        }

        UserGoogleServiceAccount account = serviceAccountRepository.findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId)
                .orElseThrow(() -> new GoogleDriveException("No active default Google Service Account configured for this user. Please configure a service account first."));

        try {
            Drive driveService = createDriveService(
                    account.getCredentialsFilePath(),
                    account.getApplicationName()
            );

            // Update connection status
            account.setConnectionStatus(UserGoogleServiceAccount.ConnectionStatus.CONNECTED);
            account.setLastConnectionCheck(LocalDateTime.now());
            account.setConnectionError(null);
            serviceAccountRepository.save(account);

            // Cache the service
            driveServicesCache.put(userId, driveService);

            return driveService;
        } catch (Exception e) {
            log.error("Failed to create Drive service for user {}: {}", userId, e.getMessage());

            // Update connection status
            account.setConnectionStatus(UserGoogleServiceAccount.ConnectionStatus.FAILED);
            account.setLastConnectionCheck(LocalDateTime.now());
            account.setConnectionError(e.getMessage());
            serviceAccountRepository.save(account);

            throw new GoogleDriveException("Failed to connect to Google Drive: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a Drive service for a specific service account of a user
     */
    public Drive getDriveServiceForAccount(Long userId, Long accountId) {
        // For specific accounts, we don't use cache to ensure we get the latest 
        // configuration directly from the database
        UserGoogleServiceAccount account = serviceAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new GoogleDriveException("Service account not found"));

        if (!account.getIsActive()) {
            throw new GoogleDriveException("Service account is not active");
        }

        try {
            Drive driveService = createDriveService(
                    account.getCredentialsFilePath(),
                    account.getApplicationName()
            );

            // Update connection status
            account.setConnectionStatus(UserGoogleServiceAccount.ConnectionStatus.CONNECTED);
            account.setLastConnectionCheck(LocalDateTime.now());
            account.setConnectionError(null);
            serviceAccountRepository.save(account);

            return driveService;
        } catch (Exception e) {
            log.error("Failed to create Drive service for account {}: {}", accountId, e.getMessage());

            // Update connection status
            account.setConnectionStatus(UserGoogleServiceAccount.ConnectionStatus.FAILED);
            account.setLastConnectionCheck(LocalDateTime.now());
            account.setConnectionError(e.getMessage());
            serviceAccountRepository.save(account);

            throw new GoogleDriveException("Failed to connect to Google Drive: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the root folder ID for a user's default service account
     */
    public String getRootFolderId(Long userId) {
        UserGoogleServiceAccount account = serviceAccountRepository.findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId)
                .orElseThrow(() -> new GoogleDriveException("No active default Google Service Account configured for this user. Please configure a service account first."));

        return account.getRootFolderId();
    }

    /**
     * Gets the root folder ID for a specific service account
     */
    public String getRootFolderIdForAccount(Long userId, Long accountId) {
        UserGoogleServiceAccount account = serviceAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new GoogleDriveException("Service account not found"));

        if (!account.getIsActive()) {
            throw new GoogleDriveException("Service account is not active");
        }

        return account.getRootFolderId();
    }

    /**
     * Creates a Drive service using the provided credentials file and
     * application name
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
     * Invalidates the cached Drive service for a user
     */
    public void invalidateCache(Long userId) {
        driveServicesCache.remove(userId);
    }

    /**
     * Utility class for creating a test Drive service
     */
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
