package com.eightsnpc.todo.api.error;

import org.springframework.http.HttpStatus;

public class CommentNotFoundException extends BusinessException {
    public CommentNotFoundException(long id) {
        super("COMMENT_NOT_FOUND", HttpStatus.NOT_FOUND, "未找到编号为 " + id + " 的评论");
    }
}
