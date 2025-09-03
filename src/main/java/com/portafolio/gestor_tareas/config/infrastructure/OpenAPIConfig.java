package com.portafolio.gestor_tareas.config.infrastructure;

import com.portafolio.gestor_tareas.exception.domain.ApiError;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "Bearer"
        )
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestor de tareas API")
                        .version("1.0")
                        .description("API para gestionar usuarios y tareas"))
                .components(new Components()
                                // Not found - 404
                                .addResponses("NotFound",
                                        new ApiResponse()
                                                .description("Resource not found")
                                                .content(new Content().addMediaType("application/json",
                                                        new MediaType().example(new ApiError(
                                                                LocalDateTime.now(),
                                                                404,
                                                                "Task with ID 5 not found",
                                                                "/api/tasks/5",
                                                                List.of()
                                                        ))
                                                ))
                                        // Bad Request - 400
                                ).addResponses("BadRequest",
                                        new ApiResponse()
                                                .description("Bad request")
                                                .content(new Content().addMediaType("application/json",
                                                        new MediaType().example(new ApiError(
                                                                LocalDateTime.now(),
                                                                400,
                                                                "Invalid input data",
                                                                "/api/tasks",
                                                                List.of()
                                                        ))
                                                ))
                                        // Unauthorized - 401
                                ).addResponses("Unauthorized",
                                        new ApiResponse()
                                                .description("Unauthorized access")
                                                .content(new Content().addMediaType("application/json",
                                                        new MediaType().example(new ApiError(
                                                                LocalDateTime.now(),
                                                                401,
                                                                "Invalid or missing token",
                                                                "/api/tasks",
                                                                List.of()
                                                        ))
                                                ))
                                        // Forbidden - 403
                                ).addResponses("Forbidden",
                                        new ApiResponse()
                                                .description("Forbidden - user lacks permissions")
                                                .content(new Content().addMediaType("application/json",
                                                        new MediaType().example(new ApiError(
                                                                LocalDateTime.now(),
                                                                403,
                                                                "You dont permission to access this resources",
                                                                "/api/admins/tasks",
                                                                List.of()
                                                        ))
                                                ))
                                        // Internal Server Error - 500
                                ).addResponses("InternalError",
                                        new ApiResponse()
                                                .description("Unexpected error")
                                                .content(new Content().addMediaType("application/json",
                                                        new MediaType().example(new ApiError(
                                                                LocalDateTime.now(),
                                                                500,
                                                                "An unexpected error occurred. Please try again later",
                                                                "/api/tasks",
                                                                List.of()
                                                        ))
                                                ))
                                )
                );

    }
}
