package com.eightsnpc.todo.api.dto;

import com.eightsnpc.todo.Project;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "项目信息")
public record ProjectResponse(long id, String name, String description) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(project.id(), project.name(), project.description());
    }
}
