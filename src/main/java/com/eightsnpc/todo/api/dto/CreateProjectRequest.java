package com.eightsnpc.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "新增项目请求")
public record CreateProjectRequest(
        @NotBlank(message = "项目名称不能为空")
        @Size(max = 100, message = "项目名称不能超过 100 个字符")
        @Schema(description = "项目名称", example = "个人学习计划")
        String name,
        @Size(max = 500, message = "项目描述不能超过 500 个字符")
        @Schema(description = "项目描述", example = "记录本周学习任务")
        String description) {
}
