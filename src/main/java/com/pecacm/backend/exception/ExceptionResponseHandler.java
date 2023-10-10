package com.pecacm.backend.exception;

import com.pecacm.backend.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class ExceptionResponseHandler {

    @ExceptionHandler(AcmException.class)
    public ResponseEntity<ErrorResponse> handleAcmException(AcmException acmException) {
        return ResponseEntity.status(acmException.getStatus()).body(new ErrorResponse(acmException.getStatus().getReasonPhrase(), acmException.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleConversionException(MethodArgumentTypeMismatchException ex) {
        String errorMessage = "Invalid parameter value. " + ex.getName() + " should be of type " + ex.getRequiredType();
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
