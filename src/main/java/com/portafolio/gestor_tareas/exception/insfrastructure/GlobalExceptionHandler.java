package com.portafolio.gestor_tareas.exception.insfrastructure;

import com.portafolio.gestor_tareas.dto.ApiError;
import com.portafolio.gestor_tareas.exception.domain.*;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String DEFAULT_ERROR_MESSAGE = "Unexpected error";

    private ApiError buildError(HttpStatus status, HttpServletRequest request, List<String> errors) {
        return ApiError.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
    }

    // Validation errors -> HTTP 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.warn("[VALIDATION ERROR]: {} {} - {}", method, path, e.getMessage(), e);

        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, request, errors));
    }

    // BadRequestException -> HTTP 400
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException e, HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.warn("[BAD REQUEST]: {} {} - {}", method, path, e.getMessage(), e);

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, request, errors));
    }

    // JSON parse errors → HTTP 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleJsonParseError(HttpMessageNotReadableException e, HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.warn("[JSON ERRORS]: {} {} - {}", method, path, e.getMessage(), e);

        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST, request, List.of("Invalid request body")));
    }

    // RefreshTokenExpiredException -> HTTP 401
    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ApiError> handleUnauthorized(RefreshTokenExpiredException e, HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.warn("[REFRESH TOKEN EXPIRED]: {} {} - {}" ,method, path, e.getMessage(), e);

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildError(HttpStatus.UNAUTHORIZED, request, errors));
    }

    // Forbidden -> HTTP 403
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleAccessDenied(ForbiddenException e, HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.warn("[FORBIDDEN]: {} {} - {}", method, path, e.getMessage(), e);

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(HttpStatus.FORBIDDEN, request, errors));
    }

    // AccessDenied -> HTTP 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleSpringSecurityAccessDenied(AccessDeniedException e, HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.warn("[ACCESS DENIED]: {} {} - {}", method, path, e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(HttpStatus.FORBIDDEN, request, List.of("Access Denied")));
    }

    // RefreshTokenRevoked -> HTTP 403
    @ExceptionHandler(RefreshTokenRevokedException.class)
    public ResponseEntity<ApiError> handleRefreshTokenRevoked(RefreshTokenRevokedException e, HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.warn("[REFRESH TOKEN REVOKED]: {} {} - {}", method, path, e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(HttpStatus.FORBIDDEN, request, List.of("Refresh token revoked")));
    }

    // NotFoundException → HTTP 404
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException e, HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.warn("[NOT FOUND]: {} {} - {}", method, path, e.getMessage(), e);

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, request, errors));
    }

    // RefreshTokenNotFoundException → HTTP 404
    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ApiError> handleRefreshTokenNotFound(RefreshTokenNotFoundException e, HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.warn("[REFRESH TOKEN NOT FOUND]: {} {} - {}", method, path, e.getMessage(), e);

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, request, errors));
    }

    // Conflict UserAlreadyExist -> HTTP 409
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserAlreadyExists(UserAlreadyExistsException e, HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.warn("[USER ALREADY EXIST]: {} {} - {}", method, path, e.getMessage(), e);

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, request, errors));
    }

    // Conflict TaskAlreadyExist -> HTTP 409
    @ExceptionHandler(TaskAlreadyExistException.class)
    public ResponseEntity<ApiError> handleTaskAlreadyExists(TaskAlreadyExistException e, HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.warn("[TASK ALREADY EXIST]: {} {} - {}", method, path, e.getMessage(), e);

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, request, errors));
    }

    // Conflict InvalidTaskCompletedException -> HTTP 409
    @ExceptionHandler(InvalidTaskCompleteException.class)
    public ResponseEntity<ApiError> handleTaskAlreadyCompleted(InvalidTaskCompleteException e, HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.warn("[INVALID TASK COMPLETED]: {} {} - {}", method, path, e.getMessage(), e);

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, request, errors));
    }

    // Conflict UserDontHavePermissions -> HTTP 409
    @ExceptionHandler(UserDontHavePermissionsException.class)
    public ResponseEntity<ApiError> handleUserDontHavePermissions(UserDontHavePermissionsException e, HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.warn("[USER DONT HAVE PERMISSIONS]: {} {} - {}", method, path, e.getMessage(), e);

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, request, errors));
    }

    // Conflict UserDontHaveTasks -> HTTP 409
    @ExceptionHandler(UserDontHaveTasksException.class)
    public ResponseEntity<ApiError> handleUserDontHaveTasks(UserDontHaveTasksException e, HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.warn("[USER DONT HAVE TASKS]: {} {} - {}", method, path, e.getMessage(), e);

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, request, errors));
    }

    // Exception -> 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception e, HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.error("[UNEXPECTED ERROR OCCURRED]: {} {} - {}", method, path, e.getMessage(), e);

        List<String> errors = List.of(e.getMessage() != null ? e.getMessage() : DEFAULT_ERROR_MESSAGE);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, request, errors));
    }
}
