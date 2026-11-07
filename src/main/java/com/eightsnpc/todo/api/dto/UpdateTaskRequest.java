package com.eightsnpc.todo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "修改任务请求")
public record UpdateTaskRequest(
        @Schema(description = "任务内容，不能为空且最多 200 个字符", example = "完成 Spring Boot 练习")
        @NotBlank(message = "任务内容不能为空")
        @Size(max = 200, message = "任务内容不能超过 200 个字符")
        String description,
        @Schema(description = "任务是否已完成", example = "true")
        @NotNull(message = "completed 字段不能为空")
        Boolean completed) {
}
