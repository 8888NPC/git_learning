package com.eightsnpc.todo;

import java.util.List;
import java.util.Optional;

public interface TaskService {
    Task add(String description, Long projectId);

    default Task add(String description) {
        return add(description, null);
    }

    Optional<Task> findById(long id);

    List<Task> list();

    PageResult<Task> page(long current, long size, Long projectId, Boolean completed, String keyword);

    boolean complete(long id);

    Optional<Task> setCompletionStatus(long id, boolean completed);

    Optional<Task> update(long id, String description, boolean completed);

    boolean delete(long id);
}
