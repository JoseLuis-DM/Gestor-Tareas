package com.portafolio.gestor_tareas.users.domain;

import com.portafolio.gestor_tareas.users.infrastructure.dto.UserDTO;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserResponseDTO;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserWithPermissionsDTO;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.List;
import java.util.Set;

public interface UserService {

    UserResponseDTO register(UserDTO userDTO);

    UserResponseDTO update(UserDTO userDTO, UserDetails userDetails);

    UserResponseDTO findById(Long id, UserDetails userDetails);

    List<UserResponseDTO> findAll();

    void delete(Long id);

    void addPermissions(Long userId, String email, Set<Permission> permissions);

    Map<String, Object> deletePermissions(
            Long id,
            String email,
            boolean allPermissions,
            Set<Permission> permissions
    );

    List<Permission> showPermissions(Long id, UserDetails userDetails);

    List<UserWithPermissionsDTO> showAllUsersWithPermissions(UserDetails userDetails);
}
