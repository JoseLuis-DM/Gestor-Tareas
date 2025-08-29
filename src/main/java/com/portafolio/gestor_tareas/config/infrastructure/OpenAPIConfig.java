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

import java.util.Map;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gestor de tareas API")
                        .version("1.0")
                        .description("API para gestionar usuarios y tareas"));
        /*
                .components(new Components()
                        // Not found - 404
                        .addResponses("NotFound",
                                new ApiResponse()
                                        .description("Resource not found")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().example(Map.of(
                                                        "status",404,
                                                        "message","Task with ID 5 not found",
                                                        "path","/api/tasks/5"
                                                ))
                                        ))
                        // Bad Request - 400
                        ).addResponses("BadRequest",
                                new ApiResponse()
                                        .description("Bad request")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().example(Map.of(
                                                        "status",400,
                                                        "message","Invalid input data",
                                                        "path","/api/tasks"
                                                ))
                                        ))
                        // Unauthorized - 401
                        ).addResponses("Unauthorized",
                                new ApiResponse()
                                        .description("Unauthorized access")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().example(Map.of(
                                                        "status", 401,
                                                        "message", "Invalid or missing token",
                                                        "path", "/api/tasks"
                                                ))
                                        ))
                        // Forbidden - 403
                        ).addResponses("Forbidden",
                                new ApiResponse()
                                        .description("Forbidden - user lacks permissions")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().example(Map.of(
                                                        "status", 403,
                                                        "message", "You dont permission to access this resources",
                                                        "path", "/api/admins/tasks"
                                                ))
                                        ))
                        // Internal Server Error - 500
                        ).addResponses("InternalError",
                                new ApiResponse()
                                        .description("Unexpected error")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().example(Map.of(
                                                        "status", 500,
                                                        "message", "An unexpected error occurred. Please try again later",
                                                        "path", "/api/tasks"
                                                ))
                                        ))
                        )
                );*/
    }
}
