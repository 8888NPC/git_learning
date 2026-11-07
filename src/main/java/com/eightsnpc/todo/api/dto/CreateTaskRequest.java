package com.eightsnpc.todo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank(message = "任务内容不能为空")
        @Size(max = 200, message = "任务内容不能超过 200 个字符")
        String description) {
}
