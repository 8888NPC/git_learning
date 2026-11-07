package com.eightsnpc.todo.api.dto;

import com.eightsnpc.todo.Task;

public record TaskResponse(long id, String description, boolean completed) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getDescription(), task.isCompleted());
    }
}
