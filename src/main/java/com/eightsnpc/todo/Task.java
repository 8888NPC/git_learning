package com.eightsnpc.todo;

public final class Task {
    private final long id;
    private final Long projectId;
    private String description;
    private boolean completed;

    public Task(long id, String description) {
        this(id, description, null);
    }

    public Task(long id, String description, Long projectId) {
        this.id = id;
        this.projectId = projectId;
        this.description = description;
        this.completed = false;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Long getProjectId() {
        return projectId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void markCompleted() {
        completed = true;
    }

    public void update(String description, boolean completed) {
        this.description = description;
        this.completed = completed;
    }

    public Task copy() {
        Task copy = new Task(id, description, projectId);
        copy.completed = completed;
        return copy;
    }
}
