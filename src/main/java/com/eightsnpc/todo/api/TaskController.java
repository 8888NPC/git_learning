package com.eightsnpc.todo.api;

import com.eightsnpc.todo.Task;
import com.eightsnpc.todo.TaskService;
import com.eightsnpc.todo.PageResult;
import com.eightsnpc.todo.api.dto.ApiResponse;
import com.eightsnpc.todo.api.dto.CreateTaskRequest;
import com.eightsnpc.todo.api.dto.TaskResponse;
import com.eightsnpc.todo.api.dto.UpdateTaskRequest;
import com.eightsnpc.todo.api.error.TaskNotFoundException;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "任务管理", description = "任务的新增、查询、修改和删除")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "新增任务")
    public ResponseEntity<ApiResponse<TaskResponse>> create(
            @Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.add(request.description(), request.projectId());
        URI location = URI.create("/api/v1/tasks/" + task.getId());
        return ResponseEntity.created(location).body(ApiResponse.success(TaskResponse.from(task)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询任务详情")
    public ApiResponse<TaskResponse> getById(@PathVariable long id) {
        return taskService.findById(id)
                .map(TaskResponse::from)
                .map(ApiResponse::success)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @GetMapping
    @Operation(summary = "查询任务列表")
    public ApiResponse<PageResult<TaskResponse>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) String keyword) {
        PageResult<Task> taskPage = taskService.page(page, size, projectId, completed, keyword);
        List<TaskResponse> records = taskPage.records().stream().map(TaskResponse::from).toList();
        return ApiResponse.success(new PageResult<>(
                taskPage.current(), taskPage.size(), taskPage.total(), records));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改任务")
    public ApiResponse<TaskResponse> update(
            @PathVariable long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update(id, request.description(), request.completed())
                .map(TaskResponse::from)
                .map(ApiResponse::success)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除任务")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable long id) {
        if (!taskService.delete(id)) {
            throw new TaskNotFoundException(id);
        }
        return ResponseEntity.ok(ApiResponse.successWithoutData());
    }
}
