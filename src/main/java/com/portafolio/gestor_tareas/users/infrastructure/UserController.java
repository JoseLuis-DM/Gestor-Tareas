package com.portafolio.gestor_tareas.users.infrastructure;

import com.portafolio.gestor_tareas.users.infrastructure.dto.UserDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserController {

    ResponseEntity<UserDTO> register(UserDTO userDTO);

    ResponseEntity<UserDTO> update(UserDTO userDTO);

    ResponseEntity<UserDTO> findById(Long id);

    ResponseEntity<List<UserDTO>> findAll();

    ResponseEntity<Void> delete(Long id);
}
