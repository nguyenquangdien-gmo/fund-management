package com.huybq.fund_management.domain.ggdrive.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.huybq.fund_management.domain.ggdrive.dto.GoogleServiceAccountRequestDTO;
import com.huybq.fund_management.domain.ggdrive.dto.GoogleServiceAccountResponseDTO;
import com.huybq.fund_management.domain.ggdrive.entity.UserGoogleServiceAccount;
import com.huybq.fund_management.domain.ggdrive.exception.GoogleDriveException;
import com.huybq.fund_management.domain.ggdrive.repository.UserGoogleServiceAccountRepository;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import com.huybq.fund_management.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserGoogleServiceAccountService {

    private final UserGoogleServiceAccountRepository serviceAccountRepository;
    private final UserRepository userRepository;
    private final GoogleServiceAccountStorageService storageService;
    private final GoogleDriveServiceFactory driveServiceFactory;

    private static final String DEFAULT_ROOT_FOLDER_ID = "root";

    /**
     * Configures a new Google Service Account for a user This method is useful
     * when setting up the first service account
     */
    @Transactional
    public GoogleServiceAccountResponseDTO configureServiceAccount(
            Long userId,
            MultipartFile credentialsFile,
            GoogleServiceAccountRequestDTO requestDTO) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if account name already exists for this user
        if (serviceAccountRepository.existsByAccountName(requestDTO.getAccountName())) {
            throw new GoogleDriveException("An account with this name already exists for this user");
        }

        // Store the credentials file with account name for better identification
        String credentialsFilePath = storageService.storeCredentialsFile(credentialsFile, requestDTO.getAccountName());

        // Set default root folder id if not provided
        String rootFolderId = StringUtils.hasText(requestDTO.getRootFolderId())
                ? requestDTO.getRootFolderId()
                : DEFAULT_ROOT_FOLDER_ID;

        try {
            // Test the connection before saving
            testConnection(credentialsFilePath, requestDTO.getApplicationName());

            // Check if this should be the default account
            boolean isDefault = requestDTO.getIsDefault();

            // If this is set as default, unset any existing default first
            if (isDefault) {
                serviceAccountRepository.findByIsDefaultTrue()
                        .ifPresent(account -> {
                            account.setIsDefault(false);
                            serviceAccountRepository.save(account);
                        });
            } else {
                // If there are no other accounts, make this the default
                List<UserGoogleServiceAccount> existingAccounts = serviceAccountRepository.findByUserId(userId);
                if (existingAccounts.isEmpty()) {
                    isDefault = true;
                }
            }

            // Create new account
            UserGoogleServiceAccount account = UserGoogleServiceAccount.builder()
                    .user(user)
                    .accountName(requestDTO.getAccountName())
                    .description(requestDTO.getDescription())
                    .credentialsFilePath(credentialsFilePath)
                    .applicationName(requestDTO.getApplicationName())
                    .rootFolderId(rootFolderId)
                    .isDefault(isDefault)
                    .isActive(true)
                    .connectionStatus(UserGoogleServiceAccount.ConnectionStatus.CONNECTED)
                    .lastConnectionCheck(LocalDateTime.now())
                    .build();

            UserGoogleServiceAccount savedAccount = serviceAccountRepository.save(account);

            // Invalidate cache if this is the default account
            if (isDefault) {
                driveServiceFactory.invalidateCache();
            }

            return convertToResponse(savedAccount);
        } catch (Exception e) {
            // If connection test fails, delete the newly stored file
            storageService.deleteCredentialsFile(credentialsFilePath);
            throw e;
        }
    }

    /**
     * Adds an additional Google Service Account for a user This method is
     * specifically for adding more accounts when a user already has one or more
     * accounts
     */
    @Transactional
    public GoogleServiceAccountResponseDTO addServiceAccount(
            Long userId,
            MultipartFile credentialsFile,
            GoogleServiceAccountRequestDTO requestDTO) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get existing accounts to check if this is the first account
        List<UserGoogleServiceAccount> existingAccounts = serviceAccountRepository.findByUserId(userId);

        // Check if account name already exists for this user
        if (serviceAccountRepository.existsByAccountName(requestDTO.getAccountName())) {
            throw new GoogleDriveException("An account with this name already exists for this user");
        }

        // Store the credentials file with account name for better identification
        String credentialsFilePath = storageService.storeCredentialsFile(credentialsFile, requestDTO.getAccountName());

        // Set default root folder id if not provided
        String rootFolderId = StringUtils.hasText(requestDTO.getRootFolderId())
                ? requestDTO.getRootFolderId()
                : DEFAULT_ROOT_FOLDER_ID;

        try {
            // Test the connection before saving
            testConnection(credentialsFilePath, requestDTO.getApplicationName());

            // Handle default account logic
            boolean isDefault = requestDTO.getIsDefault();

            // If making this account default, we need to unset the current default
            if (isDefault) {
                serviceAccountRepository.findByIsDefaultTrue()
                        .ifPresent(account -> {
                            account.setIsDefault(false);
                            serviceAccountRepository.save(account);
                        });
            } else if (existingAccounts.isEmpty()) {
                // If no accounts exist yet, make this one the default regardless
                isDefault = true;
            }

            // Create new account
            UserGoogleServiceAccount account = UserGoogleServiceAccount.builder()
                    .user(user)
                    .accountName(requestDTO.getAccountName())
                    .description(requestDTO.getDescription())
                    .credentialsFilePath(credentialsFilePath)
                    .applicationName(requestDTO.getApplicationName())
                    .rootFolderId(rootFolderId)
                    .isDefault(isDefault)
                    .isActive(true) // New accounts are active by default
                    .connectionStatus(UserGoogleServiceAccount.ConnectionStatus.CONNECTED)
                    .lastConnectionCheck(LocalDateTime.now())
                    .build();

            UserGoogleServiceAccount savedAccount = serviceAccountRepository.save(account);

            // Invalidate cache if this is the default account
            if (isDefault) {
                driveServiceFactory.invalidateCache();
            }

            return convertToResponse(savedAccount);
        } catch (Exception e) {
            // If connection test fails, delete the newly stored file
            storageService.deleteCredentialsFile(credentialsFilePath);
            throw new GoogleDriveException("Failed to add service account: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing Google Service Account
     */
    @Transactional
    public GoogleServiceAccountResponseDTO updateServiceAccount(
            Long accountId,
            GoogleServiceAccountRequestDTO requestDTO,
            MultipartFile credentialsFile) {

        UserGoogleServiceAccount account = serviceAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Service account not found"));

        // Check if name already exists for a different account
        if (!account.getAccountName().equals(requestDTO.getAccountName())
                && serviceAccountRepository.existsByAccountName(requestDTO.getAccountName())) {
            throw new GoogleDriveException("An account with this name already exists");
        }

        String credentialsFilePath = account.getCredentialsFilePath();

        // If new credentials file is provided
        if (credentialsFile != null && !credentialsFile.isEmpty()) {
            // Store the new credentials file with account name for better identification
            String newCredentialsPath = storageService.storeCredentialsFile(credentialsFile, requestDTO.getAccountName());

            try {
                // Test connection with new credentials
                testConnection(newCredentialsPath, requestDTO.getApplicationName());

                // Delete old credentials file if exists and different from new one
                if (credentialsFilePath != null && !credentialsFilePath.equals(newCredentialsPath)) {
                    storageService.deleteCredentialsFile(credentialsFilePath);
                }

                credentialsFilePath = newCredentialsPath;
            } catch (Exception e) {
                // If connection test fails, delete the newly stored file
                storageService.deleteCredentialsFile(newCredentialsPath);
                throw e;
            }
        } else if (!account.getApplicationName().equals(requestDTO.getApplicationName())) {
            // If application name changed, test connection with existing credentials
            testConnection(credentialsFilePath, requestDTO.getApplicationName());
        }

        // Check if this should be the default account
        boolean isDefault = requestDTO.getIsDefault();
        boolean wasDefault = account.getIsDefault();

        // If making this account default but it wasn't already, unset the current default
        if (isDefault && !wasDefault) {
            serviceAccountRepository.findByIsDefaultTrue()
                    .ifPresent(defaultAccount -> {
                        if (!defaultAccount.getId().equals(accountId)) {
                            defaultAccount.setIsDefault(false);
                            serviceAccountRepository.save(defaultAccount);
                        }
                    });
        } else if (!isDefault && wasDefault) {
            // If removing default status, ensure there's another default account
            List<UserGoogleServiceAccount> otherAccounts = serviceAccountRepository.findByIdNot(accountId);
            if (!otherAccounts.isEmpty()) {
                // Make another account default
                UserGoogleServiceAccount newDefaultAccount = otherAccounts.get(0);
                newDefaultAccount.setIsDefault(true);
                serviceAccountRepository.save(newDefaultAccount);
            } else {
                // This is the only account, it must remain default
                isDefault = true;
            }
        }

        // Update account
        account.setAccountName(requestDTO.getAccountName());
        account.setDescription(requestDTO.getDescription());
        account.setApplicationName(requestDTO.getApplicationName());
        account.setCredentialsFilePath(credentialsFilePath);

        if (StringUtils.hasText(requestDTO.getRootFolderId())) {
            account.setRootFolderId(requestDTO.getRootFolderId());
        }

        account.setIsDefault(isDefault);
        account.setConnectionStatus(UserGoogleServiceAccount.ConnectionStatus.CONNECTED);
        account.setLastConnectionCheck(LocalDateTime.now());
        account.setConnectionError(null);

        UserGoogleServiceAccount savedAccount = serviceAccountRepository.save(account);

        // Invalidate cache if default status changed
        if (isDefault != wasDefault) {
            driveServiceFactory.invalidateCache();
        }

        return convertToResponse(savedAccount);
    }

    /**
     * Gets all service account configurations for a user
     */
    public List<GoogleServiceAccountResponseDTO> getServiceAccounts() {
        return serviceAccountRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets a specific service account by ID
     */
    public GoogleServiceAccountResponseDTO getServiceAccountById(Long accountId) {
        return serviceAccountRepository.findById(accountId)
                .map(this::convertToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Service account not found"));
    }

    /**
     * Gets the default service account for a user
     */
    public GoogleServiceAccountResponseDTO getDefaultServiceAccount() {
        return serviceAccountRepository.findByIsDefaultTrue()
                .map(this::convertToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No default service account found"));
    }

    /**
     * Sets an account as the default for a user
     */
    @Transactional
    public GoogleServiceAccountResponseDTO setDefaultServiceAccount( Long accountId) {
        UserGoogleServiceAccount account = serviceAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Service account not found"));

        if (!account.getIsActive()) {
            throw new GoogleDriveException("Cannot set inactive account as default");
        }

        // Only update if it's not already the default
        if (!account.getIsDefault()) {
            // Unset any existing default
            serviceAccountRepository.findByIsDefaultTrue()
                    .ifPresent(defaultAccount -> {
                        defaultAccount.setIsDefault(false);
                        serviceAccountRepository.save(defaultAccount);
                    });

            // Set new default
            account.setIsDefault(true);
            serviceAccountRepository.save(account);

            // Invalidate cache
            driveServiceFactory.invalidateCache();
        }

        return convertToResponse(account);
    }

    /**
     * Disables a specific service account
     */
    @Transactional
    public void disableServiceAccount(Long accountId) {
        UserGoogleServiceAccount account = serviceAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Service account not found"));

        if (account.getIsDefault()) {
            // Count total accounts
            List<UserGoogleServiceAccount> activeAccounts = serviceAccountRepository.findByIsActiveTrue();

            if (activeAccounts.size() <= 1) {
                throw new GoogleDriveException("Cannot disable the only active account. Add another account first.");
            }

            // Find another account to make default
            UserGoogleServiceAccount newDefaultAccount = activeAccounts.stream()
                    .filter(acc -> !acc.getId().equals(accountId))
                    .findFirst()
                    .orElseThrow(() -> new GoogleDriveException("Cannot find another active account to set as default"));

            // Set the new default account
            newDefaultAccount.setIsDefault(true);
            serviceAccountRepository.save(newDefaultAccount);

            // Unset default on the account being disabled
            account.setIsDefault(false);
            // Invalidate cache since default account changed
            driveServiceFactory.invalidateCache();
        }

        account.setIsActive(false);
        serviceAccountRepository.save(account);
    }

    /**
     * Enables a specific service account
     */
    @Transactional
    public GoogleServiceAccountResponseDTO enableServiceAccount( Long accountId) {
        UserGoogleServiceAccount account = serviceAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Service account not found"));

        // Test connection before enabling
        try {
            testConnection(account.getCredentialsFilePath(), account.getApplicationName());

            account.setIsActive(true);
            account.setConnectionStatus(UserGoogleServiceAccount.ConnectionStatus.CONNECTED);
            account.setLastConnectionCheck(LocalDateTime.now());
            account.setConnectionError(null);

            // If no other default account exists, make this the default
            if (!serviceAccountRepository.existsByIsDefaultTrue()) {
                account.setIsDefault(true);
                driveServiceFactory.invalidateCache();
            }

            UserGoogleServiceAccount savedAccount = serviceAccountRepository.save(account);

            return convertToResponse(savedAccount);
        } catch (Exception e) {
            account.setConnectionStatus(UserGoogleServiceAccount.ConnectionStatus.FAILED);
            account.setLastConnectionCheck(LocalDateTime.now());
            account.setConnectionError(e.getMessage());
            serviceAccountRepository.save(account);

            throw new GoogleDriveException("Failed to enable service account: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a service account
     */
    @Transactional
    public void deleteServiceAccount(Long accountId) {
        UserGoogleServiceAccount account = serviceAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Service account not found"));

        boolean wasDefault = account.getIsDefault();

        if (wasDefault) {
            // Count total accounts
            List<UserGoogleServiceAccount> otherActiveAccounts = serviceAccountRepository.findByIdNotAndIsActiveTrue(accountId);

            if (otherActiveAccounts.isEmpty()) {
                throw new GoogleDriveException("Cannot delete the only default active account. Add another account first.");
            }

            // Find another account to make default
            UserGoogleServiceAccount newDefaultAccount = otherActiveAccounts.get(0);
            newDefaultAccount.setIsDefault(true);
            serviceAccountRepository.save(newDefaultAccount);

            // Invalidate cache since default account changed
            driveServiceFactory.invalidateCache();
        }

        // Delete credentials file
        if (account.getCredentialsFilePath() != null) {
            storageService.deleteCredentialsFile(account.getCredentialsFilePath());
        }

        // Delete account
        serviceAccountRepository.delete(account);
    }

    /**
     * Tests a connection to Google Drive using the provided credentials
     */
    private void testConnection(String credentialsFilePath, String applicationName) {
        try {
            Drive testDrive = createTestDriveService(credentialsFilePath, applicationName);
            // Try to get account info to verify connection
            About about = testDrive.about().get().setFields("user").execute();
            log.info("Successfully connected to Google Drive as: {}", about.getUser().getEmailAddress());
        } catch (Exception e) {
            log.error("Failed to connect to Google Drive: {}", e.getMessage());
            throw new GoogleDriveException("Failed to connect to Google Drive: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a test Drive service using the provided credentials file and
     * application name
     */
    private Drive createTestDriveService(String credentialsFilePath, String applicationName) {
        try {
            return new GoogleDriveServiceFactory.TestDriveBuilder(credentialsFilePath, applicationName).build();
        } catch (Exception e) {
            throw new GoogleDriveException("Failed to create test Drive service", e);
        }
    }

    /**
     * Converts a UserGoogleServiceAccount entity to a response DTO
     */
    private GoogleServiceAccountResponseDTO convertToResponse(UserGoogleServiceAccount account) {
        return GoogleServiceAccountResponseDTO.builder()
                .id(account.getId())
                .accountName(account.getAccountName())
                .description(account.getDescription())
                .applicationName(account.getApplicationName())
                .rootFolderId(account.getRootFolderId())
                .isDefault(account.getIsDefault())
                .isActive(account.getIsActive())
                .connectionStatus(account.getConnectionStatus())
                .lastConnectionCheck(account.getLastConnectionCheck())
                .connectionError(account.getConnectionError())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
