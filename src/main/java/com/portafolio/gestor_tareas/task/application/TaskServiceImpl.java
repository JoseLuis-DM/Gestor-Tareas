package com.portafolio.gestor_tareas.task.application;

import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.task.domain.Task;
import com.portafolio.gestor_tareas.task.domain.TaskRepository;
import com.portafolio.gestor_tareas.task.domain.TaskService;
import com.portafolio.gestor_tareas.task.infrastructure.mapper.TaskMapper;
import com.portafolio.gestor_tareas.task.infrastructure.repository.SpringTaskRepository;
import com.portafolio.gestor_tareas.users.domain.UserRepository;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final SpringTaskRepository springTaskRepository;
    private final SpringUserRepository userRepository;

    @Override
    @Transactional
    public Task save(Task task, Long userId) {

        if (springTaskRepository.findByUserIdAndTitleIgnoreCase(
                userId, task.getTitle()).isPresent())    {
            throw new IllegalArgumentException("The task already exists");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        task.setUser(user);

        return taskRepository.save(task);
    }


    @Transactional
    public Task update(Task task, Long userId) {

        Task updateTask = taskRepository.findById(task.getId())
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (!updateTask.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You cannot update a task that is not yours");
        }

        updateTask.setTitle(task.getTitle());
        updateTask.setDescription(task.getDescription());
        updateTask.setCompleted(task.isCompleted());

        return taskRepository.save(updateTask);
    }

    @Override
    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    @Override
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}
