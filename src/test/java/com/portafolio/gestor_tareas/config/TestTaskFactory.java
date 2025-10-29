package com.portafolio.gestor_tareas.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.entity.TaskEntity;
import com.portafolio.gestor_tareas.task.infrastructure.repository.SpringTaskRepository;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TestTaskFactory {

    @Autowired
    private SpringTaskRepository springTaskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public TaskEntity createTask(UserEntity user, String title) {

        TaskEntity task = new TaskEntity();
        task.setTitle(title + "-" + UUID.randomUUID());
        task.setDescription("Description: " + task.getTitle());
        task.setCompleted(false);
        task.setUser(user);
        return springTaskRepository.save(task);
    }

    public TaskDTO createTaskDTO(String title, String description) {
        return new TaskDTO(null, title, description, false, null, false);
    }

    public TaskDTO createTaskDTOWithId(Long id, String title, String description, Long userId) {
        return new TaskDTO(id, title, description, false, userId, false);
    }

    public TaskDTO createUnassignedfTaskDTO(String title, String description) {
        return new TaskDTO(null, title, description, false, null, false);
    }

    public TaskDTO createOwnTask(String title, String description) {
        return new TaskDTO(null, title, description, false, null, true);
    }

    public TaskDTO createAssingedTaskDTO(String title, String description, Long userID) {
        return new TaskDTO(null, title, description, false, userID, false);
    }
}
