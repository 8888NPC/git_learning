package com.eightsnpc.todo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eightsnpc.todo.PageResult;
import com.eightsnpc.todo.Task;
import com.eightsnpc.todo.TaskService;
import com.eightsnpc.todo.api.error.ProjectNotFoundException;
import com.eightsnpc.todo.persistence.entity.TaskEntity;
import com.eightsnpc.todo.persistence.mapper.ProjectMapper;
import com.eightsnpc.todo.persistence.mapper.TaskMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DatabaseTaskService implements TaskService {
    private final TaskMapper taskMapper;
    private final ProjectMapper projectMapper;

    public DatabaseTaskService(TaskMapper taskMapper, ProjectMapper projectMapper) {
        this.taskMapper = taskMapper;
        this.projectMapper = projectMapper;
    }

    @Override
    public Task add(String description, Long projectId) {
        validateDescription(description);
        if (projectId != null && projectMapper.selectById(projectId) == null) {
            throw new ProjectNotFoundException(projectId);
        }

        TaskEntity entity = new TaskEntity();
        entity.setProjectId(projectId);
        entity.setDescription(description.trim());
        entity.setCompleted(false);
        taskMapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Task> findById(long id) {
        return Optional.ofNullable(taskMapper.selectById(id)).map(DatabaseTaskService::toDomain);
    }

    @Override
    public List<Task> list() {
        return taskMapper.selectList(new LambdaQueryWrapper<TaskEntity>()
                        .orderByAsc(TaskEntity::getId))
                .stream()
                .map(DatabaseTaskService::toDomain)
                .toList();
    }

    @Override
    public PageResult<Task> page(
            long current, long size, Long projectId, Boolean completed, String keyword) {
        validatePage(current, size);
        LambdaQueryWrapper<TaskEntity> query = new LambdaQueryWrapper<TaskEntity>()
                .eq(projectId != null, TaskEntity::getProjectId, projectId)
                .eq(completed != null, TaskEntity::getCompleted, completed)
                .like(keyword != null && !keyword.isBlank(), TaskEntity::getDescription, keyword)
                .orderByAsc(TaskEntity::getId);
        Page<TaskEntity> result = taskMapper.selectPage(new Page<>(current, size), query);
        List<Task> records = result.getRecords().stream().map(DatabaseTaskService::toDomain).toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public boolean complete(long id) {
        return setCompletionStatus(id, true).isPresent();
    }

    @Override
    public Optional<Task> setCompletionStatus(long id, boolean completed) {
        TaskEntity entity = taskMapper.selectById(id);
        if (entity == null) {
            return Optional.empty();
        }
        entity.setCompleted(completed);
        taskMapper.updateById(entity);
        return Optional.of(toDomain(entity));
    }

    @Override
    public Optional<Task> update(long id, String description, boolean completed) {
        validateDescription(description);
        TaskEntity entity = taskMapper.selectById(id);
        if (entity == null) {
            return Optional.empty();
        }
        entity.setDescription(description.trim());
        entity.setCompleted(completed);
        taskMapper.updateById(entity);
        return Optional.of(toDomain(entity));
    }

    @Override
    public boolean delete(long id) {
        return taskMapper.deleteById(id) > 0;
    }

    private static Task toDomain(TaskEntity entity) {
        Task task = new Task(entity.getId(), entity.getDescription(), entity.getProjectId());
        if (Boolean.TRUE.equals(entity.getCompleted())) {
            task.markCompleted();
        }
        return task;
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("任务内容不能为空");
        }
        if (description.length() > 200) {
            throw new IllegalArgumentException("任务内容不能超过 200 个字符");
        }
    }

    private void validatePage(long current, long size) {
        if (current < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException("分页参数必须满足 page >= 1 且 size 在 1 到 100 之间");
        }
    }
}
