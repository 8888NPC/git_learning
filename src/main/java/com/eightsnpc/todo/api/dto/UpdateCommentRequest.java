package com.eightsnpc.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "修改评论请求")
public record UpdateCommentRequest(
        @NotBlank(message = "评论内容不能为空")
        @Size(max = 1000, message = "评论内容不能超过 1000 个字符")
        @Schema(description = "评论正文", example = "已完成、测试通过")
        String content) {
}
