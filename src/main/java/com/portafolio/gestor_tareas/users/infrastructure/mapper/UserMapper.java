package com.portafolio.gestor_tareas.users.infrastructure.mapper;

import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserDTO;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import org.springframework.web.bind.annotation.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    User userEntityToUser(UserEntity userEntity);

    UserEntity userToUserEntity(User user);

    UserDTO userToUserDTO(User user);

    User userDTOToUser(UserDTO userDTO);

    User registerRequestToUser(RegisterRequest registerRequest);
}
