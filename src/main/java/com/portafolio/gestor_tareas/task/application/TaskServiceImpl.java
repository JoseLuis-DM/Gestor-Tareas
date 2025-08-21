package com.portafolio.gestor_tareas.task.application;

import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.task.domain.Task;
import com.portafolio.gestor_tareas.task.domain.TaskRepository;
import com.portafolio.gestor_tareas.task.domain.TaskService;
import com.portafolio.gestor_tareas.task.infrastructure.mapper.TaskMapper;
import com.portafolio.gestor_tareas.task.infrastructure.repository.SpringTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final SpringTaskRepository springTaskRepository;

    @Override
    public Task save(Task task) {

        if (springTaskRepository.findByUserIdAndTitleIgnoreCase(task.getUser().getId(), task.getTitle()).isPresent()) {
            throw new IllegalArgumentException("The task already exists");
        }

        return taskRepository.save(task);
    }

    @Override
    public Task update(Task task) {

        Task updateTask = taskRepository.findById(task.getId())
                .orElseThrow(() -> new NotFoundException("Task not found"));

        BeanUtils.copyProperties(task, updateTask, "id");

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
