package com.eightsnpc.todo.api.dto;

import com.eightsnpc.todo.Task;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "任务信息")
public record TaskResponse(long id, String description, boolean completed) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getDescription(), task.isCompleted());
    }
}
