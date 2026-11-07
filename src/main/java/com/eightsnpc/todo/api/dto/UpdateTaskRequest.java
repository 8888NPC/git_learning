package com.eightsnpc.todo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @NotBlank(message = "任务内容不能为空")
        @Size(max = 200, message = "任务内容不能超过 200 个字符")
        String description,
        @NotNull(message = "completed 字段不能为空")
        Boolean completed) {
}
