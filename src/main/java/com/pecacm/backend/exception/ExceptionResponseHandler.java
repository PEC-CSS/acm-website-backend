package com.pecacm.backend.exception;

import com.pecacm.backend.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionResponseHandler {

    @ExceptionHandler(AcmException.class)
    public ResponseEntity<ErrorResponse> handleAcmException(AcmException acmException) {
        return ResponseEntity.status(acmException.getStatus()).body(new ErrorResponse(acmException.getStatus().getReasonPhrase(), acmException.getMessage()));
    }
}
