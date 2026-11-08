package com.eightsnpc.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "项目及其首个任务")
public record ProjectWithFirstTaskResponse(ProjectResponse project, TaskResponse firstTask) {
}
