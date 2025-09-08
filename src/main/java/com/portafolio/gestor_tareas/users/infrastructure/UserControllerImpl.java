package com.portafolio.gestor_tareas.users.infrastructure;

import com.portafolio.gestor_tareas.dto.ApiResponseDTO;
import com.portafolio.gestor_tareas.dto.ApiResponseFactory;
import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.domain.UserService;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserDTO;
import com.portafolio.gestor_tareas.users.infrastructure.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Users", description = "The User API. Contains all operations on users")
@RequiredArgsConstructor
public class UserControllerImpl implements UserController{

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Register a new user",
            description = "Creates a new user in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", ref = "BadRequest", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponseDTO<UserDTO>> register(@Valid @RequestBody UserDTO userDTO) {
        User user = userMapper.userDTOToUser(userDTO);

        User register = userService.register(user);

        UserDTO registerDTO = userMapper.userToUserDTO(register);

        return ApiResponseFactory.created(registerDTO, "User created successfully");
    }

    @Operation(summary = "Update an existing user",
            description = "Updates user details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", ref = "BadRequest", content = @Content),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @PutMapping
    public ResponseEntity<ApiResponseDTO<UserDTO>> update(@Valid @RequestBody UserDTO userDTO) {
        User user = userMapper.userDTOToUser(userDTO);

        User update = userService.update(user);

        UserDTO updateDTO = userMapper.userToUserDTO(update);

        return ApiResponseFactory.success(updateDTO, "User updated successfully");
    }

    @Operation(summary = "Find user by ID",
            description = "Retrieve a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UserDTO>> findById(@PathVariable Long id) throws NotFoundException {

        User user = userService.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserDTO userDTO = userMapper.userToUserDTO(user);
        return ApiResponseFactory.success(userDTO, "User found");
    }

    @Operation(summary = "List all users",
            description = "Returns a list of all users (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found"),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<UserDTO>>> findAll() {

        List<UserDTO> userDTOS = userService.findAll().stream()
                .map(userMapper::userToUserDTO).toList();

        return ApiResponseFactory.success(userDTOS, "Users found");
    }

    @Operation(summary = "Delete user by id",
            description = "Deletes a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable Long id) {

        userService.delete(id);

        return ApiResponseFactory.success(null, "User deleted");
    }
}
