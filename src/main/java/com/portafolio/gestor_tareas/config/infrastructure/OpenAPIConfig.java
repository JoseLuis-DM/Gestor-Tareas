package com.portafolio.gestor_tareas.config.infrastructure;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "Bearer"
        )
@Configuration
public class OpenAPIConfig {

    private static final String JSON = "application/json";
    private static final String DATE = "2025-10-13T13:00:00";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestor de tareas API")
                        .version("1.0")
                        .description("API para gestionar usuarios y tareas"))
                .components(new Components()
                                // Validation errors -> 400
                                .addResponses("ValidationError",
                                        new ApiResponse()
                                                .description("Validation failed for request body or parameters")
                                                .content(new Content().addMediaType(JSON,
                                                        new MediaType().example(
                                                                builderExample(
                                                                        400,
                                                                        "Bad Request",
                                                                        DATE,
                                                                        "/api/user/register",
                                                                        List.of("Password: Password is required")
                                                                )
                                                        ))
                                                ))
                                // Bad request -> 400
                                .addResponses("BadRequest",
                                        new ApiResponse()
                                                .description("Bad request")
                                                .content(new Content().addMediaType(JSON,
                                                        new MediaType().example(
                                                                builderExample(
                                                                        400,
                                                                        "Bad Request",
                                                                        DATE,
                                                                        "/api/task",
                                                                        List.of("Bad request exception")
                                                                )
                                                        ))
                                                ))
                                // JSON parse errors -> 400
                                .addResponses("InvalidJson",
                                        new ApiResponse()
                                                .description("Invalid JSON in request body")
                                                .content(new Content().addMediaType(JSON,
                                                        new MediaType().example(
                                                                builderExample(
                                                                        400,
                                                                        "Bad Request",
                                                                        DATE,
                                                                        "/api/task/{id}/complete",
                                                                        List.of("Invalid request body")
                                                                )
                                                        ))
                                                ))
                                // Unauthorized - 401
                                .addResponses("Unauthorized",
                                        new ApiResponse()
                                                .description("Unauthorized access - invalid or missing token")
                                                .content(new Content().addMediaType(JSON,
                                                        new MediaType().example(
                                                                builderExample(
                                                                        401,
                                                                        "Unauthorized",
                                                                        DATE,
                                                                        "/api/user",
                                                                        List.of("Invalid or missing token")
                                                                )
                                                        ))
                                                ))
                                // RefreshTokenExpiredException - 401
                                .addResponses("RefreshTokenExpired",
                                        new ApiResponse()
                                                .description("Unauthorized access - RefreshToken expired")
                                                .content(new Content().addMediaType(JSON,
                                                        new MediaType().example(
                                                                builderExample(
                                                                        401,
                                                                        "Unauthorized",
                                                                        DATE,
                                                                        "/api/auth/refresh",
                                                                        List.of("RefreshToken expired. Please log in again")
                                                                )
                                                        ))
                                                ))
                                // Forbidden - 403
                                .addResponses("Forbidden",
                                        new ApiResponse()
                                                .description("Forbidden - user lacks permissions")
                                                .content(new Content().addMediaType(JSON,
                                                        new MediaType().example(
                                                                builderExample(
                                                                        403,
                                                                        "Forbidden",
                                                                        DATE,
                                                                        "/api/task",
                                                                        List.of("You don't have permission to access this resource")
                                                                )
                                                        ))
                                                ))
                                // AccessDenied -> 403
                                .addResponses("AccessDenied",
                                        new ApiResponse()
                                                .description("Forbidden - user lacks permissions")
                                                .content(new Content().addMediaType(JSON,
                                                        new MediaType().example(
                                                                builderExample(
                                                                        403,
                                                                        "Forbidden",
                                                                        DATE,
                                                                        "/api/task",
                                                                        List.of("Forbidden")
                                                                )
                                                        ))
                                                ))
                                // RefreshTokenRevoked -> 403
                                .addResponses("RefreshTokenRevoked",
                                        new ApiResponse()
                                                .description("Forbidden - RefreshToken revoked")
                                                .content(new Content().addMediaType(JSON,
                                                        new MediaType().example(
                                                                builderExample(
                                                                        403,
                                                                        "Forbidden",
                                                                        DATE,
                                                                        "/api/auth/refresh",
                                                                        List.of("RefreshToken revoked")
                                                                )
                                                        ))
                                                ))
                                // Not found - 404
                                .addResponses("NotFound",
                                        new ApiResponse()
                                                .description("Resource not found")
                                                .content(new Content().addMediaType(JSON,
                                                        new MediaType().example(
                                                                builderExample(
                                                                        404,
                                                                        "Not Found",
                                                                        DATE,
                                                                        "/api/task/5",
                                                                        List.of("Task with ID 5 not found")
                                                                )
                                                        ))
                                                ))
                                // RefreshTokenNotFoundException - 404
                                .addResponses("RefreshTokenNotFoundException",
                                        new ApiResponse()
                                                .description("Not Found - RefreshToken does not exist")
                                                .content(new Content().addMediaType(JSON,
                                                        new MediaType().example(
                                                                builderExample(
                                                                        404,
                                                                        "Not Found",
                                                                        DATE,
                                                                        "/api/task/5",
                                                                        List.of("RefreshToken not found")
                                                                )
                                                        ))
                                                ))
                                // UserAlreadyExists -> 409
                                .addResponses("UserAlreadyExists",
                                        new ApiResponse()
                                                .description("Conflict - User already exists")
                                                .content(new Content().addMediaType(JSON,
                                                        new MediaType().example(
                                                                builderExample(
                                                                        409,
                                                                        "Conflict",
                                                                        DATE,
                                                                        "/api/user/register",
                                                                        List.of("User already exists. Email is taken")
                                                                )
                                                        ))
                                                ))
                                // TaskAlreadyExist -> 409
                                .addResponses("TaskAlreadyExists",
                                        new ApiResponse()
                                                .description("Conflict - Task already exists")
                                                .content(new Content().addMediaType(JSON,
                                                        new MediaType().example(
                                                                builderExample(
                                                                        409,
                                                                        "Conflict",
                                                                        DATE,
                                                                        "/api/task",
                                                                        List.of("Task already exists")
                                                                )
                                                        ))
                                                ))
                                // InvalidTaskCompleteException -> 409
                                .addResponses("InvalidTaskComplete",
                                        new ApiResponse()
                                                .description("Conflict - Invalid task completion status")
                                                .content(new Content().addMediaType(JSON,
                                                        new MediaType().example(
                                                                builderExample(
                                                                        409,
                                                                        "Conflict",
                                                                        DATE,
                                                                        "/api/task/1/complete",
                                                                        List.of("Invalid task completion status. The task is already completed")
                                                                )
                                                        ))
                                                ))
                                // Internal Server Error - 500
                                .addResponses("InternalError",
                                        new ApiResponse()
                                                .description("Unexpected error")
                                                .content(new Content().addMediaType(JSON,
                                                        new MediaType().example(
                                                                builderExample(
                                                                        500,
                                                                        "Internal Server Error",
                                                                        DATE,
                                                                        "/api/task",
                                                                        List.of("An unexpected error occurred. Please try again later")
                                                                )
                                                        ))
                                                )
                                )
                );

    }

    private static Map<String, Object> builderExample(
            int status,
            String error,
            String timestamp,
            String path,
            List<String> errors
    ) {
        return Map.of(
                "status", status,
                "error", error,
                "timestamp", timestamp,
                "path", path,
                "errors", errors
        );
    }
}
