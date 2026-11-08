package com.eightsnpc.todo.service;

import com.eightsnpc.todo.Comment;
import com.eightsnpc.todo.CommentService;
import com.eightsnpc.todo.Task;
import com.eightsnpc.todo.TaskCommentCreationResult;
import com.eightsnpc.todo.TaskService;
import com.eightsnpc.todo.TaskWorkflowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseTaskWorkflowService implements TaskWorkflowService {
    private final TaskService taskService;
    private final CommentService commentService;

    public DatabaseTaskWorkflowService(TaskService taskService, CommentService commentService) {
        this.taskService = taskService;
        this.commentService = commentService;
    }

    @Override
    @Transactional
    public TaskCommentCreationResult createWithFirstComment(
            String description, Long projectId, String firstCommentContent) {
        Task task = taskService.add(description, projectId);
        Comment comment = commentService.create(task.getId(), firstCommentContent);
        return new TaskCommentCreationResult(task, comment);
    }
}
