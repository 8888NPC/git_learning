package com.eightsnpc.todo.api.error;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(long id) {
        super("未找到编号为 " + id + " 的任务");
    }
}
