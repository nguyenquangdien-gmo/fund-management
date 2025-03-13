package com.huybq.fund_management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private record ErrorResponse(int status, String message, String path, LocalDateTime timestamp) {}

    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception ex, HttpStatus status, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(DueAlreadyPaidException.class)
    public ResponseEntity<ErrorResponse> handleDueAlreadyPaidException(DueAlreadyPaidException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(DueNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDueNotFoundException(DueNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(FundException.class)
    public ResponseEntity<ErrorResponse> handleFundException(FundException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(FundNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFundNotFoundException(FundNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAmountException(InvalidAmountException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}

