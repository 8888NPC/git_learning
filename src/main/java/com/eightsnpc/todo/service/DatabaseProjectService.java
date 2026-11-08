package com.eightsnpc.todo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eightsnpc.todo.PageResult;
import com.eightsnpc.todo.Project;
import com.eightsnpc.todo.ProjectCreationResult;
import com.eightsnpc.todo.ProjectService;
import com.eightsnpc.todo.Task;
import com.eightsnpc.todo.TaskService;
import com.eightsnpc.todo.persistence.entity.ProjectEntity;
import com.eightsnpc.todo.persistence.mapper.ProjectMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseProjectService implements ProjectService {
    private final ProjectMapper projectMapper;
    private final TaskService taskService;

    public DatabaseProjectService(ProjectMapper projectMapper, TaskService taskService) {
        this.projectMapper = projectMapper;
        this.taskService = taskService;
    }

    @Override
    public Project create(String name, String description) {
        validateName(name);
        ProjectEntity entity = new ProjectEntity();
        entity.setName(name.trim());
        entity.setDescription(normalizeDescription(description));
        projectMapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    @Transactional
    public ProjectCreationResult createWithFirstTask(
            String name, String description, String firstTaskDescription) {
        Project project = create(name, description);
        Task firstTask = taskService.add(firstTaskDescription, project.id());
        return new ProjectCreationResult(project, firstTask);
    }

    @Override
    public Optional<Project> findById(long id) {
        return Optional.ofNullable(projectMapper.selectById(id)).map(DatabaseProjectService::toDomain);
    }

    @Override
    public PageResult<Project> page(long current, long size, String nameKeyword) {
        validatePage(current, size);
        LambdaQueryWrapper<ProjectEntity> query = new LambdaQueryWrapper<ProjectEntity>()
                .like(nameKeyword != null && !nameKeyword.isBlank(), ProjectEntity::getName, nameKeyword)
                .orderByAsc(ProjectEntity::getId);
        Page<ProjectEntity> result = projectMapper.selectPage(new Page<>(current, size), query);
        List<Project> records = result.getRecords().stream().map(DatabaseProjectService::toDomain).toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public Optional<Project> update(long id, String name, String description) {
        validateName(name);
        ProjectEntity entity = projectMapper.selectById(id);
        if (entity == null) {
            return Optional.empty();
        }
        entity.setName(name.trim());
        entity.setDescription(normalizeDescription(description));
        projectMapper.updateById(entity);
        return Optional.of(toDomain(entity));
    }

    @Override
    public boolean delete(long id) {
        return projectMapper.deleteById(id) > 0;
    }

    private static Project toDomain(ProjectEntity entity) {
        return new Project(entity.getId(), entity.getName(), entity.getDescription());
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("项目名称不能为空");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("项目名称不能超过 100 个字符");
        }
    }

    private String normalizeDescription(String description) {
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("项目描述不能超过 500 个字符");
        }
        return description == null || description.isBlank() ? null : description.trim();
    }

    private void validatePage(long current, long size) {
        if (current < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException("分页参数必须满足 page >= 1 且 size 在 1 到 100 之间");
        }
    }
}
