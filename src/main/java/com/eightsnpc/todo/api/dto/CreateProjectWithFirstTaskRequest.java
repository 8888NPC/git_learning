package com.eightsnpc.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "创建项目并附带首个任务的请求")
public record CreateProjectWithFirstTaskRequest(
        @NotBlank(message = "项目名称不能为空")
        @Size(max = 100, message = "项目名称不能超过 100 个字符")
        @Schema(description = "项目名称", example = "Spring Boot 学习")
        String name,
        @Size(max = 500, message = "项目描述不能超过 500 个字符")
        @Schema(description = "项目描述", example = "学习 Spring Boot 基础")
        String description,
        @NotBlank(message = "首个任务内容不能为空")
        @Size(max = 200, message = "首个任务内容不能超过 200 个字符")
        @Schema(description = "首个任务内容", example = "完成 MySQL 配置")
        String firstTaskDescription) {
}
