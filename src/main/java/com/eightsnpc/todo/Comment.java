package com.eightsnpc.todo;

import java.time.LocalDateTime;

public record Comment(long id, long taskId, String content, LocalDateTime createdAt) {
}
