package com.huybq.fund_management.domain.ggdrive.controller;

import com.huybq.fund_management.domain.ggdrive.dto.GoogleServiceAccountRequestDTO;
import com.huybq.fund_management.domain.ggdrive.dto.GoogleServiceAccountResponseDTO;
import com.huybq.fund_management.domain.ggdrive.exception.GoogleDriveException;
import com.huybq.fund_management.domain.ggdrive.service.UserGoogleServiceAccountService;
import com.huybq.fund_management.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/${server.version}/drive/service-accounts")
@RequiredArgsConstructor
public class UserGoogleServiceAccountController {

    private final UserGoogleServiceAccountService serviceAccountService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GoogleServiceAccountResponseDTO> configureServiceAccount(
            @RequestParam("credentials") MultipartFile credentialsFile,
            @RequestParam("accountName") String accountName,
            @RequestParam("applicationName") String applicationName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "rootFolderId", required = false) String rootFolderId,
            @RequestParam(value = "isDefault", defaultValue = "false") Boolean isDefault,
            @AuthenticationPrincipal User user) {

        log.info("Configuring service account {} for user: {}", accountName, user.getUsername());

        GoogleServiceAccountRequestDTO requestDTO = GoogleServiceAccountRequestDTO.builder()
                .accountName(accountName)
                .description(description)
                .applicationName(applicationName)
                .rootFolderId(rootFolderId)
                .isDefault(isDefault)
                .build();

        GoogleServiceAccountResponseDTO responseDTO = serviceAccountService.configureServiceAccount(
                user.getId(), credentialsFile, requestDTO);

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GoogleServiceAccountResponseDTO> addServiceAccount(
            @RequestParam("credentials") MultipartFile credentialsFile,
            @RequestParam("accountName") String accountName,
            @RequestParam("applicationName") String applicationName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "rootFolderId", required = false) String rootFolderId,
            @RequestParam(value = "isDefault", defaultValue = "false") Boolean isDefault,
            @AuthenticationPrincipal User user) {

        log.info("Adding service account {} for user: {}", accountName, user.getUsername());

        GoogleServiceAccountRequestDTO requestDTO = GoogleServiceAccountRequestDTO.builder()
                .accountName(accountName)
                .description(description)
                .applicationName(applicationName)
                .rootFolderId(rootFolderId)
                .isDefault(isDefault)
                .build();

        GoogleServiceAccountResponseDTO responseDTO = serviceAccountService.addServiceAccount(
                user.getId(), credentialsFile, requestDTO);

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/{accountId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GoogleServiceAccountResponseDTO> updateServiceAccount(
            @PathVariable Long accountId,
            @RequestParam("accountName") String accountName,
            @RequestParam("applicationName") String applicationName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "rootFolderId", required = false) String rootFolderId,
            @RequestParam(value = "isDefault", defaultValue = "false") Boolean isDefault,
            @RequestParam(value = "credentials", required = false) MultipartFile credentialsFile) {

        GoogleServiceAccountRequestDTO requestDTO = GoogleServiceAccountRequestDTO.builder()
                .accountName(accountName)
                .description(description)
                .applicationName(applicationName)
                .rootFolderId(rootFolderId)
                .isDefault(isDefault)
                .build();

        GoogleServiceAccountResponseDTO responseDTO = serviceAccountService.updateServiceAccount( accountId, requestDTO, credentialsFile);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<GoogleServiceAccountResponseDTO>> getUserServiceAccounts() {

        try {
            List<GoogleServiceAccountResponseDTO> responseDTOs = serviceAccountService.getServiceAccounts();

            if (responseDTOs.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDTOs);
            }

            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            log.error("Error getting service accounts: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{accountId}")
    public ResponseEntity<GoogleServiceAccountResponseDTO> getServiceAccountById(
            @PathVariable Long accountId) {

        try {
            GoogleServiceAccountResponseDTO responseDTO = serviceAccountService.getServiceAccountById(accountId);
            return ResponseEntity.ok(responseDTO);
        } catch (GoogleDriveException e) {
            log.error("Error getting service account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/default")
    public ResponseEntity<GoogleServiceAccountResponseDTO> getDefaultServiceAccount() {

        try {
            GoogleServiceAccountResponseDTO responseDTO = serviceAccountService.getDefaultServiceAccount();
            return ResponseEntity.ok(responseDTO);
        } catch (GoogleDriveException e) {
            log.error("Error getting default service account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{accountId}/set-default")
    public ResponseEntity<GoogleServiceAccountResponseDTO> setDefaultServiceAccount(
            @PathVariable Long accountId) {

        try {
            GoogleServiceAccountResponseDTO responseDTO = serviceAccountService.setDefaultServiceAccount(
                    accountId);
            return ResponseEntity.ok(responseDTO);
        } catch (GoogleDriveException e) {
            log.error("Error setting default service account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{accountId}/disable")
    public ResponseEntity<Void> disableServiceAccount(
            @PathVariable Long accountId) {

        try {
            serviceAccountService.disableServiceAccount(accountId);
            return ResponseEntity.ok().build();
        } catch (GoogleDriveException e) {
            log.error("Error disabling service account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{accountId}/enable")
    public ResponseEntity<GoogleServiceAccountResponseDTO> enableServiceAccount(
            @PathVariable Long accountId) {

        try {
            GoogleServiceAccountResponseDTO responseDTO = serviceAccountService.enableServiceAccount(
                    accountId);
            return ResponseEntity.ok(responseDTO);
        } catch (GoogleDriveException e) {
            log.error("Error enabling service account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteServiceAccount(
            @PathVariable Long accountId) {

        try {
            serviceAccountService.deleteServiceAccount(accountId);
            return ResponseEntity.ok().build();
        } catch (GoogleDriveException e) {
            log.error("Error deleting service account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @ExceptionHandler(GoogleDriveException.class)
    public ResponseEntity<String> handleGoogleDriveException(GoogleDriveException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}
