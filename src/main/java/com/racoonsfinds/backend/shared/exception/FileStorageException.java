package com.racoonsfinds.backend.shared.exception;

public class FileStorageException extends RuntimeException {
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}