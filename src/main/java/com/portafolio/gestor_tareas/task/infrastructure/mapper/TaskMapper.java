package com.portafolio.gestor_tareas.task.infrastructure.mapper;

import com.portafolio.gestor_tareas.task.domain.Task;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.entity.TaskEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {

    @Mapping(source = "user.id", target = "user.id")
    Task taskEntityToTask(TaskEntity taskEntity);

    @Mapping(source = "user.id", target = "user.id")
    TaskEntity taskToTaskEntity(Task task);

    @Mapping(source = "user.id", target = "userId")
    TaskDTO taskToTaskDTO(Task task);

    @Mapping(source = "userId", target = "user.id")
    Task taskDTOToTask(TaskDTO taskDTO);
}
