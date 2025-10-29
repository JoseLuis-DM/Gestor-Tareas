package com.portafolio.gestor_tareas.task.infrastructure.repository;

import com.portafolio.gestor_tareas.task.domain.Task;
import com.portafolio.gestor_tareas.task.domain.TaskRepository;
import com.portafolio.gestor_tareas.task.infrastructure.entity.TaskEntity;
import com.portafolio.gestor_tareas.task.infrastructure.mapper.TaskMapper;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MySqlTaskRepository implements TaskRepository {

    private final SpringTaskRepository springTaskRepository;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    @Override
    public Task save(Task task) {
        TaskEntity entity = taskMapper.taskToTaskEntity(task);
        TaskEntity saved = springTaskRepository.save(entity);
        return taskMapper.taskEntityToTask(saved);
    }

    @Override
    public Optional<Task> findById(Long id) {
        return springTaskRepository.findById(id)
                .map(taskMapper::taskEntityToTask);
    }

    @Override
    public List<Task> findAll() {
        return springTaskRepository.findAll()
                .stream().map(taskMapper::taskEntityToTask).toList();
    }

    @Override
    public void deleteById(Long id) {
        springTaskRepository.deleteById(id);
    }

    @Override
    public Optional<Task> findByUserIdAndTitleIgnoreCase(Long userId, String title) {
        return springTaskRepository.findByUserIdAndTitleIgnoreCase(userId, title)
                .map(taskMapper::taskEntityToTask);
    }

    @Override
    public List<Task> findByUser(User user) {
        UserEntity userEntity = userMapper.userToUserEntity(user);

        return springTaskRepository.findByUser(userEntity)
                .stream().map(taskMapper::taskEntityToTask).toList();
    }
}
