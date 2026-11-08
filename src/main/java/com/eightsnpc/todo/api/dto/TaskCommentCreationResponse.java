package com.eightsnpc.todo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "新建任务及首条评论")
public record TaskCommentCreationResponse(TaskResponse task, CommentResponse firstComment) {
}
