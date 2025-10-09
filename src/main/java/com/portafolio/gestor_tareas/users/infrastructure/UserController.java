package com.portafolio.gestor_tareas.users.infrastructure;

import com.portafolio.gestor_tareas.dto.ApiResponseDTO;
import com.portafolio.gestor_tareas.users.domain.Permission;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Set;

public interface UserController {

    ResponseEntity<ApiResponseDTO<UserDTO>> register(UserDTO userDTO);

    ResponseEntity<ApiResponseDTO<UserDTO>> update(UserDTO userDTO, UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<UserDTO>> findById(Long id, UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<List<UserDTO>>> findAll();

    ResponseEntity<ApiResponseDTO<Void>> delete(Long id, UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<Object>> addPermissionsById(Long userId, Set<Permission> permissions);

    ResponseEntity<ApiResponseDTO<Object>> addPermissionsByEmail(String email, Set<Permission> permissions);
}
