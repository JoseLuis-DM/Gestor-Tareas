package com.portafolio.gestor_tareas.task.infrastructure.mapper;

import com.portafolio.gestor_tareas.task.domain.Task;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.entity.TaskEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {

    Task taskEntityToTask(TaskEntity taskEntity);

    TaskEntity taskToTaskEntity(Task task);

    TaskDTO taskToTaskDTO(Task task);

    Task taskDTOToTask(TaskDTO taskDTO);
}
