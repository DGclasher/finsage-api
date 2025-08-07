package org.finsage.api.controllers;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class CustomErrorController {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleBindErrors(MethodArgumentNotValidException ex,
            WebRequest request) {
        logger.warn("Validation error in request {}: {}", request.getDescription(false), ex.getMessage());

        List<Map<String, String>> errorList = ex.getFieldErrors().stream()
                .map(fieldError -> {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put("field", fieldError.getField());
                    errorMap.put("message", fieldError.getDefaultMessage());
                    errorMap.put("rejectedValue", String.valueOf(fieldError.getRejectedValue()));
                    return errorMap;
                }).collect(Collectors.toList());

        Map<String, Object> errorResponse = createErrorResponse(
                "VALIDATION_ERROR",
                "Validation failed for one or more fields",
                HttpStatus.BAD_REQUEST,
                request);
        errorResponse.put("fieldErrors", errorList);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, Object>> handleJPAViolations(TransactionSystemException ex, WebRequest request) {
        logger.error("Transaction system error in request {}: {}", request.getDescription(false), ex.getMessage());

        ResponseEntity.BodyBuilder responseEntity = ResponseEntity.badRequest();
        if (ex.getCause() != null && ex.getCause().getCause() instanceof ConstraintViolationException) {
            ConstraintViolationException ve = (ConstraintViolationException) ex.getCause().getCause();
            List<Map<String, String>> errors = ve.getConstraintViolations().stream()
                    .map(constraintViolation -> {
                        Map<String, String> errorMap = new HashMap<>();
                        errorMap.put("field", constraintViolation.getPropertyPath().toString());
                        errorMap.put("message", constraintViolation.getMessage());
                        errorMap.put("rejectedValue", String.valueOf(constraintViolation.getInvalidValue()));
                        return errorMap;
                    }).collect(Collectors.toList());

            Map<String, Object> errorResponse = createErrorResponse(
                    "CONSTRAINT_VIOLATION",
                    "Database constraint violation",
                    HttpStatus.BAD_REQUEST,
                    request);
            errorResponse.put("constraintViolations", errors);

            return responseEntity.body(errorResponse);
        }

        Map<String, Object> errorResponse = createErrorResponse(
                "TRANSACTION_ERROR",
                "Transaction processing failed",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex,
            WebRequest request) {
        logger.error("Data integrity violation in request {}: {}", request.getDescription(false), ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "DATA_INTEGRITY_VIOLATION",
                "Data integrity constraint violated. This operation would result in invalid data state.",
                HttpStatus.CONFLICT,
                request);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        logger.warn("Access denied for request {}: {}", request.getDescription(false), ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "ACCESS_DENIED",
                "You don't have permission to access this resource",
                HttpStatus.FORBIDDEN,
                request);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        logger.warn("Bad credentials in request {}: {}", request.getDescription(false), ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "BAD_CREDENTIALS",
                "Invalid credentials provided",
                HttpStatus.UNAUTHORIZED,
                request);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
            WebRequest request) {
        logger.warn("Type mismatch in request {}: {}", request.getDescription(false), ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "TYPE_MISMATCH",
                String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                        ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName()),
                HttpStatus.BAD_REQUEST,
                request);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        logger.error("Runtime exception in request {}: {}", request.getDescription(false), ex.getMessage(), ex);

        Map<String, Object> errorResponse = createErrorResponse(
                "RUNTIME_ERROR",
                "An unexpected error occurred while processing your request",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, WebRequest request) {
        logger.error("Unhandled exception in request {}: {}", request.getDescription(false), ex.getMessage(), ex);

        Map<String, Object> errorResponse = createErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An internal server error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private Map<String, Object> createErrorResponse(String errorCode, String message, HttpStatus status,
            WebRequest request) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        return errorResponse;
    }
}
