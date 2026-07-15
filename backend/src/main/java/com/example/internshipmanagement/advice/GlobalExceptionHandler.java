package com.example.internshipmanagement.advice;

import com.example.internshipmanagement.dto.response.common.ApiDataResponse;
import com.example.internshipmanagement.dto.response.common.ValidationError;
import com.example.internshipmanagement.constant.ErrorMessages;
import com.example.internshipmanagement.exception.ExternalApiException;
import com.example.internshipmanagement.exception.RateLimitExceededException;
import com.example.internshipmanagement.exception.ResourceConflictException;
import com.example.internshipmanagement.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiDataResponse<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        List<ValidationError> validationErrors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
                    return ValidationError.builder()
                            .field(fieldName)
                            .message(error.getDefaultMessage())
                            .build();
                })
                .collect(Collectors.toList());

        log.warn("Validation failed: {} error(s) - {}", validationErrors.size(),
                validationErrors.stream()
                        .map(e -> e.getField() + ": " + e.getMessage())
                        .collect(Collectors.joining(", ")));

        ApiDataResponse<Void> response = ApiDataResponse.<Void>builder()
                .success(false)
                .message("Du lieu khong hop le")
                .data(null)
                .errors(validationErrors)
                .timestamp(LocalDateTime.now())
                .httpStatus(HttpStatus.BAD_REQUEST)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiDataResponse<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ApiDataResponse<String> response = ApiDataResponse.<String>builder()
                .success(false)
                .message(ex.getMessage())
                .httpStatus(HttpStatus.BAD_REQUEST)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiDataResponse<String>> handleResourceConflictException(ResourceConflictException ex) {
        log.warn("Resource conflict: {}", ex.getMessage());

        ApiDataResponse<String> response = ApiDataResponse.<String>builder()
                .success(false)
                .message(ex.getMessage())
                .httpStatus(HttpStatus.CONFLICT)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiDataResponse<String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());

        String message = ErrorMessages.UNEXPECTED_ERROR;
        if (ex.getMessage() != null && ex.getMessage().contains("violates not-null constraint")) {
            message = "Du lieu gui len thieu truong bat buoc, vui long kiem tra lai";
        }

        ApiDataResponse<String> response = ApiDataResponse.<String>builder()
                .success(false)
                .message(message)
                .httpStatus(HttpStatus.CONFLICT)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiDataResponse<String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        ApiDataResponse<String> response = ApiDataResponse.<String>builder()
                .success(false)
                .message(ex.getMessage())
                .httpStatus(HttpStatus.NOT_FOUND)
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Note: This handler is unreachable when CustomAccessDeniedHandler is configured
    // in SecurityConfig. It serves as a fallback in case that handler is removed.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiDataResponse<String>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        ApiDataResponse<String> response = ApiDataResponse.<String>builder()
                .success(false)
                .message("Ban khong co quyen thuc hien hanh dong nay: " + ex.getMessage())
                .httpStatus(HttpStatus.FORBIDDEN)
                .build();

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // Note: BadCredentialsException/DisabledException may also be caught by
    // Spring Security's AuthenticationEntryPoint before reaching this handler.
    // This handler serves as a fallback for scenarios where those exceptions
    // propagate to the controller layer.
    @ExceptionHandler({
            BadCredentialsException.class,
            DisabledException.class
    })
    public ResponseEntity<ApiDataResponse<String>> handleAuthenticationException(Exception ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        ApiDataResponse<String> response = ApiDataResponse.<String>builder()
                .success(false)
                .message(ex.getMessage())
                .httpStatus(HttpStatus.UNAUTHORIZED)
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiDataResponse<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Invalid JSON request body: {}", ex.getMessage());

        ApiDataResponse<String> response = ApiDataResponse.<String>builder()
                .success(false)
                .message("Dinh dang du lieu JSON yeu cau khong hop le")
                .httpStatus(HttpStatus.BAD_REQUEST)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiDataResponse<String>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("Method argument type mismatch: param='{}', value='{}', requiredType='{}'",
                ex.getName(), ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ApiDataResponse<String> response = ApiDataResponse.<String>builder()
                .success(false)
                .message(String.format("Tham so '%s' co gia tri khong dung dinh dang yeu cau", ex.getName()))
                .httpStatus(HttpStatus.BAD_REQUEST)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiDataResponse<String>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("HTTP method not supported: {}", ex.getMessage());

        ApiDataResponse<String> response = ApiDataResponse.<String>builder()
                .success(false)
                .message(String.format("Phuong thuc '%s' khong duoc ho tro cho duong dan nay", ex.getMethod()))
                .httpStatus(HttpStatus.METHOD_NOT_ALLOWED)
                .build();

        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiDataResponse<String>> handleExternalApiException(ExternalApiException ex) {
        log.error("External API error: {}", ex.getMessage(), ex);

        ApiDataResponse<String> response = ApiDataResponse.<String>builder()
                .success(false)
                .message(ex.getMessage())
                .httpStatus(HttpStatus.BAD_GATEWAY)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiDataResponse<String>> handleRateLimitExceededException(RateLimitExceededException ex) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());

        ApiDataResponse<String> response = ApiDataResponse.<String>builder()
                .success(false)
                .message(ex.getMessage())
                .httpStatus(HttpStatus.TOO_MANY_REQUESTS)
                .build();

        return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiDataResponse<String>> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        log.warn("Missing request header: {}", ex.getHeaderName());

        ApiDataResponse<String> response = ApiDataResponse.<String>builder()
                .success(false)
                .message(String.format("Thieu header bat buoc: '%s'. Vui long cung cap API key.", ex.getHeaderName()))
                .httpStatus(HttpStatus.BAD_REQUEST)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiDataResponse<String>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.warn("File upload too large: {}", ex.getMessage());

        ApiDataResponse<String> response = ApiDataResponse.<String>builder()
                .success(false)
                .message("File upload vuot qua kich thuoc toi da cho phep (10MB)")
                .httpStatus(HttpStatus.PAYLOAD_TOO_LARGE)
                .build();

        return new ResponseEntity<>(response, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiDataResponse<String>> handleGeneralException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ApiDataResponse<String> response = ApiDataResponse.<String>builder()
                .success(false)
                .message(ErrorMessages.UNEXPECTED_ERROR)
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


