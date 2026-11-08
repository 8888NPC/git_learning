package com.eightsnpc.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "修改任务完成状态请求")
public record UpdateTaskStatusRequest(
        @NotNull(message = "completed 字段不能为空")
        @Schema(description = "是否完成", example = "true")
        Boolean completed) {
}
