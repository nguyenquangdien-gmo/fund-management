package com.huybq.fund_management.domain.ggdrive.exception;

public class FolderNotFoundException extends GoogleDriveException {

    public FolderNotFoundException(Long folderId) {
        super("Folder not found with ID: " + folderId);
    }
}
