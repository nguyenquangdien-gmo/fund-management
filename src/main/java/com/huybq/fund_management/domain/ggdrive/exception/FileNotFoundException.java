package com.huybq.fund_management.domain.ggdrive.exception;

public class FileNotFoundException extends GoogleDriveException {

    public FileNotFoundException(Long fileId) {
        super("File not found with ID: " + fileId);
    }
}
