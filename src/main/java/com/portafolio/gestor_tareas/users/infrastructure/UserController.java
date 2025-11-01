package com.portafolio.gestor_tareas.users.infrastructure;

import com.portafolio.gestor_tareas.dto.ApiResponseDTO;
import com.portafolio.gestor_tareas.users.domain.Permission;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserDTO;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserResponseDTO;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserWithPermissionsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface UserController {

    ResponseEntity<ApiResponseDTO<UserResponseDTO>> register(UserDTO userDTO);

    ResponseEntity<ApiResponseDTO<UserResponseDTO>> update(UserDTO userDTO, UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<UserResponseDTO>> findById(Long id, UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<List<UserResponseDTO>>> findAll();

    ResponseEntity<ApiResponseDTO<Void>> delete(Long id, UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<Object>> addPermissionsById(Long userId, Set<Permission> permissions);

    ResponseEntity<ApiResponseDTO<Object>> addPermissionsByEmail(String email, Set<Permission> permissions);

    ResponseEntity<ApiResponseDTO<Map<String, Object>>> deletePermissionsById(
            Long userId,
            boolean allPermissions,
            Set<Permission> permissions
    );

    ResponseEntity<ApiResponseDTO<Map<String, Object>>> deletePermissionsByEmail(
            String email,
            boolean allPermissions,
            Set<Permission> permissions
    );

    ResponseEntity<ApiResponseDTO<List<Permission>>> showPermissionsById(Long id, UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<List<UserWithPermissionsDTO>>> showAllUsersWithPermissions(UserDetails userDetails);
}
