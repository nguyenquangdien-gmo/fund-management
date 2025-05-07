package com.huybq.fund_management.domain.ggdrive.exception;

public class FolderAlreadyExistsException extends GoogleDriveException {

    public FolderAlreadyExistsException(String folderName, Long parentFolderId) {
        super("Folder '" + folderName + "' already exists in parent folder with ID: " + parentFolderId);
    }

    public FolderAlreadyExistsException(String folderName) {
        super("Folder '" + folderName + "' already exists in root folder");
    }
}
