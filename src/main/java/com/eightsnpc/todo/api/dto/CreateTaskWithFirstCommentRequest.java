package com.eightsnpc.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "创建任务并附带首条评论的请求")
public record CreateTaskWithFirstCommentRequest(
        @NotBlank(message = "任务内容不能为空")
        @Size(max = 200, message = "任务内容不能超过 200 个字符")
        @Schema(description = "任务内容", example = "完成综合项目开发")
        String description,
        @Schema(description = "所属项目编号，可选", example = "1")
        Long projectId,
        @NotBlank(message = "评论内容不能为空")
        @Size(max = 1000, message = "评论内容不能超过 1000 个字符")
        @Schema(description = "首条评论", example = "已经开始处理")
        String firstCommentContent) {
}
