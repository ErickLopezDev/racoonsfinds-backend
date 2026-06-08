package com.racoonsfinds.backend.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * @deprecated Use {@link NotFoundException} instead.
 */
@Deprecated
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String msg) {
        super(msg, HttpStatus.NOT_FOUND);
    }
}
