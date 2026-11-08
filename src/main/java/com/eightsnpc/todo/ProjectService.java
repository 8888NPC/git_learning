package com.eightsnpc.todo;

import java.util.Optional;

public interface ProjectService {
    Project create(String name, String description);

    ProjectCreationResult createWithFirstTask(String name, String description, String firstTaskDescription);

    Optional<Project> findById(long id);

    PageResult<Project> page(long current, long size, String nameKeyword);

    Optional<Project> update(long id, String name, String description);

    boolean delete(long id);
}
