package com.portafolio.gestor_tareas.config.infrastructure;

import com.portafolio.gestor_tareas.exception.domain.ApiError;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                                                        404,
                                                        "Task with ID 5 not found",
                                                        "/api/tasks/5"
                                                ))
                                        ))
                        // Bad Request - 400
                        ).addResponses("BadRequest",
                                new ApiResponse()
                                        .description("Bad request")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().example(new ApiError(
                                                        400,
                                                        "Invalid input data",
                                                        "/api/tasks"
                                                ))
                                        ))
                        // Unauthorized - 401
                        ).addResponses("Unauthorized",
                                new ApiResponse()
                                        .description("Unauthorized access")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().example(new ApiError(
                                                        401,
                                                        "Invalid or missing token",
                                                        "/api/tasks"
                                                ))
                                        ))
                        // Forbidden - 403
                        ).addResponses("Forbidden",
                                new ApiResponse()
                                        .description("Forbidden - user lacks permissions")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().example(new ApiError(
                                                        403,
                                                        "You dont permission to access this resources",
                                                        "/api/admins/tasks"
                                                ))
                                        ))
                        // Internal Server Error - 500
                        ).addResponses("InternalError",
                                new ApiResponse()
                                        .description("Unexpected error")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().example(new ApiError(
                                                        500,
                                                        "An unexpected error occurred. Please try again later",
                                                        "/api/tasks"
                                                ))
                                        ))
                        )
                );
    }
}
