package com.portafolio.gestor_tareas.exception.insfrastructure;

import com.portafolio.gestor_tareas.dto.ApiError;
import com.portafolio.gestor_tareas.exception.domain.*;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String DEFAULT_ERROR_MESSAGE = "Unexpected error";

    // Validation errors -> HTTP 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {

        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }


    // BadRequestException -> HTTP 400
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException e, HttpServletRequest request) {

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    // JSON parse errors → HTTP 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleJsonParseError(HttpMessageNotReadableException e, HttpServletRequest request) {
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(List.of("Invalid request body"))
                .build();

        return ResponseEntity.badRequest().body(apiError);
    }

    // RefreshTokenExpiredException -> HTTP 401
    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ApiError> handleUnauthorized(RefreshTokenExpiredException e, HttpServletRequest request) {

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    // Forbidden -> HTTP 403
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleAccessDenied(ForbiddenException e, HttpServletRequest request) {

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    // AccessDenied -> HTTP 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleSpringSecurityAccessDenied(AccessDeniedException e, HttpServletRequest request) {

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(List.of("Forbidden"))
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    // RefreshTokenRevoked -> HTTP 403
    @ExceptionHandler(RefreshTokenRevokedException.class)
    public ResponseEntity<ApiError> handleRefreshTokenRevoked(RefreshTokenRevokedException e, HttpServletRequest request) {

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(List.of("Forbidden"))
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    // NotFoundException → HTTP 404
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException e, HttpServletRequest request) {

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    // RefreshTokenNotFoundException → HTTP 404
    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ApiError> handleRefreshTokenNotFound(RefreshTokenNotFoundException e, HttpServletRequest request) {

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    // Conflict UserAlreadyExist -> HTTP 409
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserAlreadyExists(UserAlreadyExistsException e, HttpServletRequest request) {

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    // Conflict TaskAlreadyExist -> HTTP 409
    @ExceptionHandler(TaskAlreadyExistException.class)
    public ResponseEntity<ApiError> handleTaskAlreadyExists(TaskAlreadyExistException e, HttpServletRequest request) {

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    // Conflict InvalidTaskCompletedException -> HTTP 409
    @ExceptionHandler(InvalidTaskCompleteException.class)
    public ResponseEntity<ApiError> handleTaskAlreadyCompleted(InvalidTaskCompleteException e, HttpServletRequest request) {

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    // Exception -> 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception e, HttpServletRequest request) {

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}
