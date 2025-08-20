package com.example.backend;

import com.example.backend.dto.ApiError;
import com.example.backend.exception.ConflictException;
import com.example.backend.exception.NotAuthorizedException;
import com.example.backend.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ApiError build(HttpStatus status, String message, HttpServletRequest req) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                req.getRequestURI()
        );
    }

    // Bean Validation: @Valid auf DTOs (Body)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst().orElse("Validation failed");
        log.debug("400 Validation (MethodArgumentNotValid): {}", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(build(HttpStatus.BAD_REQUEST, msg, req));
    }

    // Deine eigene ValidationException (SERVICE-Ebene)
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleCustomValidation(ValidationException ex, HttpServletRequest req) {
        log.debug("400 Validation (Custom): {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(build(HttpStatus.BAD_REQUEST, ex.getMessage(), req));
    }

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<ApiError> handleCustomValidation(NotAuthorizedException ex, HttpServletRequest req) {
        log.debug("403 UNAUTHORIZED (Custom): {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(build(HttpStatus.CONFLICT, ex.getMessage(), req));
    }

    // Konflikte (z.B. Unique-Constraint)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("409 Conflict: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(build(HttpStatus.CONFLICT, "Conflict", req));
    }

    // ResponseStatusException respektieren (z.B. 401/403/404)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleRse(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = ex.getStatusCode() instanceof HttpStatus hs ? hs : HttpStatus.valueOf(ex.getStatusCode().value());
        log.debug("{} RSE: {}", status.value(), ex.getReason());
        String msg = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return ResponseEntity.status(status).body(build(status, msg, req));
    }

    // Fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletRequest req) {
        log.error("500 Internal Server Error at {}: {}", req.getRequestURI(), ex.toString(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", req));
    }
}
