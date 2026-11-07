package com.eightsnpc.todo.api;

import com.eightsnpc.todo.Task;
import com.eightsnpc.todo.TaskService;
import com.eightsnpc.todo.api.dto.CreateTaskRequest;
import com.eightsnpc.todo.api.dto.TaskResponse;
import com.eightsnpc.todo.api.dto.UpdateTaskRequest;
import com.eightsnpc.todo.api.error.TaskNotFoundException;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.add(request.description());
        URI location = URI.create("/api/v1/tasks/" + task.getId());
        return ResponseEntity.created(location).body(TaskResponse.from(task));
    }

    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable long id) {
        return taskService.findById(id)
                .map(TaskResponse::from)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @GetMapping
    public List<TaskResponse> list() {
        return taskService.list().stream().map(TaskResponse::from).toList();
    }

    @PutMapping("/{id}")
    public TaskResponse update(
            @PathVariable long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update(id, request.description(), request.completed())
                .map(TaskResponse::from)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        if (!taskService.delete(id)) {
            throw new TaskNotFoundException(id);
        }
        return ResponseEntity.noContent().build();
    }
}
