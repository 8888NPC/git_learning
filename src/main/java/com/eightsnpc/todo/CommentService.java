package com.eightsnpc.todo;

import java.util.Optional;

public interface CommentService {
    Comment create(long taskId, String content);

    PageResult<Comment> pageByTask(long taskId, long current, long size);

    Optional<Comment> findById(long id);

    Optional<Comment> update(long id, String content);

    boolean delete(long id);
}
