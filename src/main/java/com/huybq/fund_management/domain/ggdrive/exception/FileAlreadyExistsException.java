package com.huybq.fund_management.domain.ggdrive.exception;

public class FileAlreadyExistsException extends GoogleDriveException {

    public FileAlreadyExistsException(String fileName, Long folderId) {
        super("File '" + fileName + "' already exists in folder with ID: " + folderId);
    }
}
