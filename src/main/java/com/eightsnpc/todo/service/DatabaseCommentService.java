package com.eightsnpc.todo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eightsnpc.todo.Comment;
import com.eightsnpc.todo.CommentService;
import com.eightsnpc.todo.PageResult;
import com.eightsnpc.todo.api.error.TaskNotFoundException;
import com.eightsnpc.todo.persistence.entity.CommentEntity;
import com.eightsnpc.todo.persistence.mapper.CommentMapper;
import com.eightsnpc.todo.persistence.mapper.TaskMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DatabaseCommentService implements CommentService {
    private final CommentMapper commentMapper;
    private final TaskMapper taskMapper;

    public DatabaseCommentService(CommentMapper commentMapper, TaskMapper taskMapper) {
        this.commentMapper = commentMapper;
        this.taskMapper = taskMapper;
    }

    @Override
    public Comment create(long taskId, String content) {
        ensureTaskExists(taskId);
        validateContent(content);

        CommentEntity entity = new CommentEntity();
        entity.setTaskId(taskId);
        entity.setContent(content.trim());
        entity.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    public PageResult<Comment> pageByTask(long taskId, long current, long size) {
        ensureTaskExists(taskId);
        validatePage(current, size);
        Page<CommentEntity> result = commentMapper.selectPage(
                new Page<>(current, size),
                new LambdaQueryWrapper<CommentEntity>()
                        .eq(CommentEntity::getTaskId, taskId)
                        .orderByDesc(CommentEntity::getId));
        List<Comment> records = result.getRecords().stream()
                .map(DatabaseCommentService::toDomain)
                .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public Optional<Comment> findById(long id) {
        return Optional.ofNullable(commentMapper.selectById(id)).map(DatabaseCommentService::toDomain);
    }

    @Override
    public Optional<Comment> update(long id, String content) {
        validateContent(content);
        CommentEntity entity = commentMapper.selectById(id);
        if (entity == null) {
            return Optional.empty();
        }
        entity.setContent(content.trim());
        commentMapper.updateById(entity);
        return Optional.of(toDomain(entity));
    }

    @Override
    public boolean delete(long id) {
        return commentMapper.deleteById(id) > 0;
    }

    private void ensureTaskExists(long taskId) {
        if (taskMapper.selectById(taskId) == null) {
            throw new TaskNotFoundException(taskId);
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (content.length() > 1000) {
            throw new IllegalArgumentException("评论内容不能超过 1000 个字符");
        }
    }

    private void validatePage(long current, long size) {
        if (current < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException("分页参数必须满足 page >= 1 且 size 在 1 到 100 之间");
        }
    }

    private static Comment toDomain(CommentEntity entity) {
        return new Comment(entity.getId(), entity.getTaskId(), entity.getContent(), entity.getCreatedAt());
    }
}
