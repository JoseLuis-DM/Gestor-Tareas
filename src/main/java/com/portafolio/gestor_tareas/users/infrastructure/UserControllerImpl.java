package com.portafolio.gestor_tareas.users.infrastructure;

import com.portafolio.gestor_tareas.dto.ApiResponseDTO;
import com.portafolio.gestor_tareas.dto.ApiResponseFactory;
import com.portafolio.gestor_tareas.exception.domain.BadRequestException;
import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.users.domain.Permission;
import com.portafolio.gestor_tareas.users.domain.UserService;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserDTO;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserResponseDTO;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserWithPermissionsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Users", description = "The User API. Contains all operations on users")
@RequiredArgsConstructor
@Slf4j
public class UserControllerImpl implements UserController{

    private final UserService userService;

    @Operation(summary = "Register a new user",
            description = "Creates a new user in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/ValidationError"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/InvalidJson"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/UserAlreadyExists"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PostMapping
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> register(
            @Valid @RequestBody UserDTO userDTO
    ) {
        log.info("POST /api/users - creating new user: {}", userDTO.getEmail());
        UserResponseDTO registerDTO = userService.register(userDTO);
        log.info("User created successfully with id {}", registerDTO.getId());
        return ApiResponseFactory.created(registerDTO, "User created successfully");
    }

    @Operation(summary = "Update an existing user",
            description = "Updates user details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/ValidationError"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PutMapping
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> update(
            @Valid @RequestBody UserDTO userDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PUT /api/users - updating user {}", userDTO.getEmail());
        UserResponseDTO updateDTO = userService.update(userDTO, userDetails);
        log.info("User {} updated successfully", updateDTO.getId());
        return ApiResponseFactory.success(updateDTO, "User updated successfully");
    }

    @Operation(summary = "Find user by ID",
            description = "Retrieve a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails)
            throws NotFoundException
    {
        log.info("GET /api/users/{} - fetching user details", id);
        UserResponseDTO userDTO = userService.findById(id, userDetails);
        log.debug("Fetched user: {}", userDTO);
        return ApiResponseFactory.success(userDTO, "User found");
    }

    @Operation(summary = "List all users",
            description = "Returns a list of all users (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found"),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<UserResponseDTO>>> findAll() {
        log.info("GET /api/users - fetching all users");
        List<UserResponseDTO> userDTOS = userService.findAll();
        log.debug("Fetched {} users", userDTOS.size());
        return ApiResponseFactory.success(userDTOS, "Users found");
    }

    @Operation(summary = "Delete user by id",
            description = "Deletes a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.warn("DELETE /api/users/{} - deleting users", id);
        userService.delete(id);
        log.info("User {} deleted successfully", id);
        return ApiResponseFactory.success(null, "User deleted");
    }

    @Operation(summary = "Add permission by ID",
            description = "Aggregation of permissions in the user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Added permits correctly"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/permissions")
    public ResponseEntity<ApiResponseDTO<Object>> addPermissionsById(
            @PathVariable("id") Long userId,
            @RequestBody Set<Permission> permissions
    ) {
        log.info("POST /api/users/{}/permissions - add permissions by id", userId);
        userService.addPermissions(userId, null, permissions);
        log.info("Permissions successfully added to user {}", userId);
        return ApiResponseFactory.success(null, "Added permits correctly");
    }

    @Operation(summary = "Add permission by email",
            description = "Aggregation of permissions in the user by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Added permits correctly"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/email/{email}/permissions")
    public ResponseEntity<ApiResponseDTO<Object>> addPermissionsByEmail(
            @PathVariable String email,
            @RequestBody Set<Permission> permissions
    ) {
        log.info("POST /api/users/email/{}/permissions - add permissions by email", email);
        userService.addPermissions(null, email, permissions);
        log.info("Permissions successfully added to user {}", email);
        return ApiResponseFactory.success(null, "Added permits correctly");
    }

    @Operation(summary = "Delete permissions by userId",
            description = "Removing a user's permissions by their user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissions removed successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/permissions")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> deletePermissionsById(
            @PathVariable Long id,
            @RequestParam boolean allPermissions,
            @RequestBody(required = false) Set<Permission> permissions
    ) {
        log.warn("DELETE /api/users/{}/permissions - delete permissions by id", id);
        if (!allPermissions && (permissions == null || permissions.isEmpty())) {
            throw new BadRequestException("You must specify which permissions you want to remove or remove all.");
        }

        Map<String, Object> response = userService.deletePermissions(id, null, allPermissions, permissions);
        log.info("Permissions successfully removed from the user {}", id);
        return ApiResponseFactory.success(response, "Permissions updated successfully");
    }

    @Operation(summary = "Delete permissions by email",
            description = "Removing a user's permissions by their email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissions removed successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/email/{email}/permissions")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> deletePermissionsByEmail(
            @PathVariable String email,
            @RequestParam boolean allPermissions,
            @RequestBody(required = false) Set<Permission> permissions
    ) {
        log.warn("DELETE /api/users/email/{}/permissions - delete permissions by email", email);
        if (!allPermissions && (permissions == null || permissions.isEmpty())) {
            throw new BadRequestException("You must specify which permissions you want to remove or remove all.");
        }

        Map<String, Object> response = userService.deletePermissions(null, email, allPermissions, permissions);
        log.info("Permissions successfully removed from the user {}", email);
        return ApiResponseFactory.success(response, "Permissions updated successfully");
    }

    @Operation(summary = "List user permissions by ID",
            description = "Returns the list of permissions assigned to a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissions found"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @GetMapping("/{id}/permissions")
    public ResponseEntity<ApiResponseDTO<List<Permission>>> showPermissionsById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("GET /api/users/{}/permissions - fetching permissions", id);
        List<Permission> permissions = userService.showPermissions(id, userDetails);

        if (permissions.isEmpty()) {
            return ApiResponseFactory.warning(permissions, "User has no assigned permissions");
        }
        log.info("Permissions from user {} found", id);
        return ApiResponseFactory.success(permissions, "Permissions found");
    }

    @Operation(summary = "List users with their permissions",
            description = "Returns the list of permissions assigned to each user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users with permissions found"),
            @ApiResponse(responseCode = "204", description = "No users with permissions found"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponseDTO<List<UserWithPermissionsDTO>>> showAllUsersWithPermissions(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("GET /api/users/permissions - fetching all users with permissions");
        List<UserWithPermissionsDTO> users = userService.showAllUsersWithPermissions(userDetails);

        if (users.isEmpty()) {
            return ApiResponseFactory.noContent("No users with assigned permissions were found");
        }
        log.info("Permissions from users found");
        return ApiResponseFactory.success(users, "Users with permissions found");
    }
}