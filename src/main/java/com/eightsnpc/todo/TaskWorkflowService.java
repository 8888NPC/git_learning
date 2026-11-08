package com.eightsnpc.todo;

public interface TaskWorkflowService {
    TaskCommentCreationResult createWithFirstComment(
            String description, Long projectId, String firstCommentContent);
}
