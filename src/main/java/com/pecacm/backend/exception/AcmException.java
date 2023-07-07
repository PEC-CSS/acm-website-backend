package com.pecacm.backend.exception;

import com.pecacm.backend.model.ErrorResponse;
import org.springframework.http.HttpStatus;

public class AcmException extends RuntimeException {
    private HttpStatus status;

    public AcmException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public AcmException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public AcmException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public AcmException(String message, Throwable cause) {
        this(message, cause, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorResponse toErrorResponse() {
        return new ErrorResponse(status.getReasonPhrase(), getMessage());
    }
}