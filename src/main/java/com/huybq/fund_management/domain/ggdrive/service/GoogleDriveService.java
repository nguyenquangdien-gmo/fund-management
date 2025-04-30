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
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFile;
import com.huybq.fund_management.domain.ggdrive.entity.DriveFolder;
import com.huybq.fund_management.domain.ggdrive.entity.GoogleDriveAccount;
import com.huybq.fund_management.domain.ggdrive.repository.DriveFileRepository;
import com.huybq.fund_management.domain.ggdrive.repository.DriveFolderRepository;
import com.huybq.fund_management.domain.ggdrive.repository.GoogleDriveAccountRepository;
import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleDriveService {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final GoogleDriveAccountRepository driveAccountRepository;
    private final DriveFolderRepository folderRepository;
    private final DriveFileRepository fileRepository;
    private final UserRepository userRepository;

    @Transactional
    public GoogleDriveAccount saveGoogleDriveAccount(GoogleDriveAccount account) {
        return driveAccountRepository.save(account);
    }

    private Drive getDriveService(GoogleDriveAccount account) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        GoogleCredentials credentials = GoogleCredentials.create(
                new AccessToken(account.getAccessToken(),
                        java.util.Date.from(account.getTokenExpiryDate().atZone(ZoneId.systemDefault()).toInstant()))
        );

        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName("Google Drive Management")
                .build();
    }

    @Transactional
    public DriveFile uploadFile(Long driveAccountId, Long folderId, Long userId, MultipartFile file)
            throws IOException, GeneralSecurityException {
        GoogleDriveAccount account = driveAccountRepository.findById(driveAccountId)
                .orElseThrow(() -> new RuntimeException("Google Drive account not found"));

        DriveFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Drive driveService = getDriveService(account);

        File fileMetadata = new File();
        fileMetadata.setName(file.getOriginalFilename());
        fileMetadata.setParents(Collections.singletonList(folder.getGoogleFolderId()));

        File uploadedFile = driveService.files().create(fileMetadata,
                        new InputStreamContent(file.getContentType(), new ByteArrayInputStream(file.getBytes())))
                .setFields("id, name, mimeType, size, webViewLink, webContentLink, createdTime, modifiedTime")
                .execute();

        // Set permission to read only
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
        driveFile.setGoogleDriveAccount(account);

        return fileRepository.save(driveFile);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listFolderContents(Long folderId) {
        DriveFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        GoogleDriveAccount account = folder.getGoogleDriveAccount();

        Map<String, Object> result = new HashMap<>();
        result.put("currentFolder", folder);
        result.put("subFolders", folderRepository.findByParentFolderId(folder.getId()));
        result.put("files", fileRepository.findByFolder(folder));

        return result;
    }

    @Transactional
    public void deleteFile(Long fileId, User user) throws IOException, GeneralSecurityException {
        DriveFile driveFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!user.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Only admin can delete files");
        }

        GoogleDriveAccount account = driveFile.getGoogleDriveAccount();
        Drive driveService = getDriveService(account);

        driveService.files().delete(driveFile.getGoogleFileId()).execute();
        fileRepository.delete(driveFile);
    }

    public byte[] downloadFile(Long fileId) throws IOException, GeneralSecurityException {
        DriveFile driveFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        GoogleDriveAccount account = driveFile.getGoogleDriveAccount();
        Drive driveService = getDriveService(account);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        driveService.files().get(driveFile.getGoogleFileId())
                .executeMediaAndDownloadTo(outputStream);

        return outputStream.toByteArray();
    }

    @Transactional(readOnly = true)
    public void refreshDriveContents(Long accountId) throws IOException, GeneralSecurityException {
        GoogleDriveAccount account = driveAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Google Drive account not found"));

        Drive driveService = getDriveService(account);

        // First get the root folder
        String rootFolderId = account.getRootFolderId();
        File rootFile = driveService.files().get(rootFolderId)
                .setFields("id, name, webViewLink")
                .execute();

        DriveFolder rootFolder = folderRepository.findByGoogleFolderId(rootFolderId)
                .orElseGet(() -> {
                    DriveFolder newFolder = new DriveFolder();
                    newFolder.setGoogleFolderId(rootFolderId);
                    newFolder.setGoogleDriveAccount(account);
                    return newFolder;
                });

        rootFolder.setName(rootFile.getName());
        rootFolder.setWebViewLink(rootFile.getWebViewLink());
        folderRepository.save(rootFolder);

        // Now recursively sync all folders and files
        syncFolder(account, driveService, rootFolder, null);
    }

    private void syncFolder(GoogleDriveAccount account, Drive driveService, DriveFolder folder, DriveFolder parentFolder)
            throws IOException {
        // Sync all subfolders
        String query = String.format("'%s' in parents and mimeType='application/vnd.google-apps.folder'",
                folder.getGoogleFolderId());

        FileList subFolders = driveService.files().list()
                .setQ(query)
                .setFields("files(id, name, webViewLink)")
                .execute();

        for (File subFolder : subFolders.getFiles()) {
            DriveFolder dbSubFolder = folderRepository.findByGoogleFolderId(subFolder.getId())
                    .orElseGet(() -> {
                        DriveFolder newFolder = new DriveFolder();
                        newFolder.setGoogleFolderId(subFolder.getId());
                        newFolder.setGoogleDriveAccount(account);
                        newFolder.setParentFolder(folder);
                        return newFolder;
                    });

            dbSubFolder.setName(subFolder.getName());
            dbSubFolder.setWebViewLink(subFolder.getWebViewLink());
            folderRepository.save(dbSubFolder);

            // Recursively sync this subfolder
            syncFolder(account, driveService, dbSubFolder, folder);
        }

        // Sync all files in this folder
        query = String.format("'%s' in parents and mimeType!='application/vnd.google-apps.folder'",
                folder.getGoogleFolderId());

        FileList files = driveService.files().list()
                .setQ(query)
                .setFields("files(id, name, mimeType, size, webViewLink, webContentLink, createdTime, modifiedTime)")
                .execute();

        for (File file : files.getFiles()) {
            DriveFile dbFile = fileRepository.findByGoogleFileId(file.getId())
                    .orElseGet(() -> {
                        DriveFile newFile = new DriveFile();
                        newFile.setGoogleFileId(file.getId());
                        newFile.setFolder(folder);
                        newFile.setGoogleDriveAccount(account);
                        return newFile;
                    });

            dbFile.setName(file.getName());
            dbFile.setMimeType(file.getMimeType());
            dbFile.setSize(file.getSize());
            dbFile.setWebViewLink(file.getWebViewLink());
            dbFile.setWebContentLink(file.getWebContentLink());

            if (file.getCreatedTime() != null) {
                dbFile.setCreatedTime(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(file.getCreatedTime().getValue()), ZoneId.systemDefault()));
            }

            if (file.getModifiedTime() != null) {
                dbFile.setModifiedTime(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(file.getModifiedTime().getValue()), ZoneId.systemDefault()));
            }

            fileRepository.save(dbFile);
        }
    }
    @Transactional(readOnly = true)
    public DriveFile getFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
    }
}

