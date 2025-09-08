package com.portafolio.gestor_tareas.users.infrastructure;

import com.portafolio.gestor_tareas.dto.ApiResponseDTO;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserController {

    ResponseEntity<ApiResponseDTO<UserDTO>> register(UserDTO userDTO);

    ResponseEntity<ApiResponseDTO<UserDTO>> update(UserDTO userDTO);

    ResponseEntity<ApiResponseDTO<UserDTO>> findById(Long id);

    ResponseEntity<ApiResponseDTO<List<UserDTO>>> findAll();

    ResponseEntity<ApiResponseDTO<Void>> delete(Long id);
}
