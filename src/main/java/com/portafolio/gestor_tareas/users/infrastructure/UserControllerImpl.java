package com.portafolio.gestor_tareas.users.infrastructure;

import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.domain.UserService;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserDTO;
import com.portafolio.gestor_tareas.users.infrastructure.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.SecurityMarker;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/users")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Users", description = "The User API. Contains all operations on users")
@RequiredArgsConstructor
public class UserControllerImpl implements UserController{

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Register a new user", description = "Creates a new user in the system")
    @PostMapping
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserDTO userDTO) {
        User user = userMapper.userDTOToUser(userDTO);

        User register = userService.register(user);

        UserDTO registerDTO = userMapper.userToUserDTO(register);

        return ResponseEntity.status(HttpStatus.CREATED).body(registerDTO);
    }

    @Operation(summary = "Update an existing user", description = "Updates user details")
    @PutMapping
    public ResponseEntity<UserDTO> update(@RequestBody UserDTO userDTO) {
        User user = userMapper.userDTOToUser(userDTO);

        User update = userService.update(user);

        UserDTO updateDTO = userMapper.userToUserDTO(update);

        return ResponseEntity.ok(updateDTO);
    }

    @Operation(summary = "Find user by ID", description = "Retrieve a user by their ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable Long id) throws NotFoundException {

        User user = userService.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserDTO userDTO = userMapper.userToUserDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @Operation(summary = "List all users", description = "Returns a list of all users (admin only)")
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserDTO>> findAll() {

        List<UserDTO> userDTOS = userService.findAll().stream()
                .map(userMapper::userToUserDTO).toList();

        return ResponseEntity.ok(userDTOS);
    }

    @Operation(summary = "Delete user by id", description = "Deletes a user by their ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        userService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
