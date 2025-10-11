package com.portafolio.gestor_tareas.task.application;

import com.portafolio.gestor_tareas.config.infrastructure.SecurityConfig;
import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.exception.domain.TaskAlreadyExistException;
import com.portafolio.gestor_tareas.task.domain.Task;
import com.portafolio.gestor_tareas.task.domain.TaskRepository;
import com.portafolio.gestor_tareas.task.domain.TaskService;
import com.portafolio.gestor_tareas.task.infrastructure.mapper.TaskMapper;
import com.portafolio.gestor_tareas.task.infrastructure.repository.SpringTaskRepository;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final SpringTaskRepository springTaskRepository;
    private final SpringUserRepository userRepository;
    private final SecurityConfig securityConfig;

    private UserEntity user;

    @Override
    @Transactional
    public Task save(Task task, Long userId) {

        if (springTaskRepository.findByUserIdAndTitleIgnoreCase(
                userId, task.getTitle()).isPresent())    {
            throw new TaskAlreadyExistException("The task already exists");
        }

        user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        task.setUser(user);

        return taskRepository.save(task);
    }

    @Transactional
    public Task update(Task task, Long userId, UserDetails userDetails) {

        Task updateTask = taskRepository.findById(task.getId())
                .orElseThrow(() -> new NotFoundException("Task not found"));

        user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        securityConfig.checkAccess(updateTask.getUser().getId(), userDetails);

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
    public void delete(Long id, UserDetails userDetails) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The task does not exist"));

        securityConfig.checkAccess(task.getUser().getId(), userDetails);

        taskRepository.deleteById(id);
    }
}
