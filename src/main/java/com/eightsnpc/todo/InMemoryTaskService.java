package com.eightsnpc.todo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class InMemoryTaskService implements TaskService {
    private final List<Task> tasks = new ArrayList<>();
    private long nextId = 1;

    @Override
    public synchronized Task add(String description, Long projectId) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("任务内容不能为空。");
        }

        Task task = new Task(nextId++, description.trim(), projectId);
        tasks.add(task);
        return task.copy();
    }

    @Override
    public synchronized Optional<Task> findById(long id) {
        return tasks.stream()
                .filter(task -> task.getId() == id)
                .findFirst()
                .map(Task::copy);
    }

    @Override
    public synchronized List<Task> list() {
        return tasks.stream().map(Task::copy).toList();
    }

    @Override
    public synchronized PageResult<Task> page(
            long current, long size, Long projectId, Boolean completed, String keyword) {
        validatePage(current, size);
        List<Task> filtered = tasks.stream()
                .filter(task -> projectId == null || projectId.equals(task.getProjectId()))
                .filter(task -> completed == null || completed == task.isCompleted())
                .filter(task -> keyword == null || keyword.isBlank()
                        || task.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .map(Task::copy)
                .collect(Collectors.toList());
        long offset = (current - 1) * size;
        List<Task> page = filtered.stream().skip(offset).limit(size).toList();
        return new PageResult<>(current, size, filtered.size(), page);
    }

    @Override
    public synchronized boolean complete(long id) {
        for (Task task : tasks) {
            if (task.getId() == id) {
                task.markCompleted();
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized Optional<Task> update(long id, String description, boolean completed) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("任务内容不能为空。");
        }

        for (Task task : tasks) {
            if (task.getId() == id) {
                task.update(description.trim(), completed);
                return Optional.of(task.copy());
            }
        }
        return Optional.empty();
    }

    @Override
    public synchronized boolean delete(long id) {
        return tasks.removeIf(task -> task.getId() == id);
    }

    private void validatePage(long current, long size) {
        if (current < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException("分页参数必须满足 page >= 1 且 size 在 1 到 100 之间");
        }
    }
}
