package com.portafolio.gestor_tareas.users.infrastructure.mapper;

import com.portafolio.gestor_tareas.auth.infrastructure.RegisterRequest;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserDTO;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.ArrayList;
import java.util.HashSet;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, builder = @Builder(disableBuilder = true))
public interface UserMapper {

    User userEntityToUser(UserEntity userEntity);

    UserEntity userToUserEntity(User user);

    @Mapping(target = "task", ignore = true)
    UserDTO userToUserDTO(User user);

    @Mapping(target = "task", ignore = true)
    default User userDTOToUser(UserDTO userDTO) {
        if (userDTO == null) return null;

        User user = new User();
        user.setId(userDTO.getId());
        user.setFirstname(userDTO.getFirstname());
        user.setLastname(userDTO.getLastname());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setRole(userDTO.getRole());

        user.setPermissions(
                userDTO.getPermissions() != null ? new HashSet<>(userDTO.getPermissions()) : new HashSet<>()
        );
        user.setTask(
                userDTO.getTask() != null ? new ArrayList<>() : new ArrayList<>()
        );

        return user;
    }


    User registerRequestToUser(RegisterRequest registerRequest);
}
