package com.eightsnpc.todo.api.error;

import org.springframework.http.HttpStatus;

public class TaskNotFoundException extends BusinessException {
    public TaskNotFoundException(long id) {
        super("TASK_NOT_FOUND", HttpStatus.NOT_FOUND, "未找到编号为 " + id + " 的任务");
    }
}
