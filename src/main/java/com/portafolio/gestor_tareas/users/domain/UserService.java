package com.portafolio.gestor_tareas.users.domain;

import com.portafolio.gestor_tareas.dto.ApiResponseDTO;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserWithPermissionsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.Set;

public interface UserService {

    User register(User user);

    User update(User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    void delete(Long id);

    void addPermissions(Long userId, String email, Set<Permission> permissions);

    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> deletePermissions(Long id, String email, boolean allPermissions, Set<Permission> permissions);

    List<Permission> showPermissions(Long id, UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<List<UserWithPermissionsDTO>>> showAllUsersWithPermissions(UserDetails userDetails);
}
