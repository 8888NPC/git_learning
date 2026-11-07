package com.eightsnpc.todo;

import java.util.List;
import java.util.Optional;

public interface TaskService {
    Task add(String description);

    Optional<Task> findById(long id);

    List<Task> list();

    boolean complete(long id);

    Optional<Task> update(long id, String description, boolean completed);

    boolean delete(long id);
}
