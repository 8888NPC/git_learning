package com.eightsnpc.todo.api.dto;

import com.eightsnpc.todo.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "评论信息")
public record CommentResponse(long id, long taskId, String content, LocalDateTime createdAt) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(comment.id(), comment.taskId(), comment.content(), comment.createdAt());
    }
}
