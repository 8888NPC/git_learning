package com.eightsnpc.todo;

public final class Task {
    private final long id;
    private String description;
    private boolean completed;

    public Task(long id, String description) {
        this.id = id;
        this.description = description;
        this.completed = false;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
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
        Task copy = new Task(id, description);
        copy.completed = completed;
        return copy;
    }
}
