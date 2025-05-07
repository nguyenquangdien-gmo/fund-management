package com.huybq.fund_management.domain.ggdrive.exception;

public class PermissionDeniedException extends GoogleDriveException {

    public PermissionDeniedException(String message) {
        super(message);
    }
}
