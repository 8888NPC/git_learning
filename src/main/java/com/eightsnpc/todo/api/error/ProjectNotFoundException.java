package com.eightsnpc.todo.api.error;

import org.springframework.http.HttpStatus;

public class ProjectNotFoundException extends BusinessException {
    public ProjectNotFoundException(long id) {
        super("PROJECT_NOT_FOUND", HttpStatus.NOT_FOUND, "未找到编号为 " + id + " 的项目");
    }
}
