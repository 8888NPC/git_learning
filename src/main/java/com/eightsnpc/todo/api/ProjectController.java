package com.eightsnpc.todo.api;

import com.eightsnpc.todo.PageResult;
import com.eightsnpc.todo.Project;
import com.eightsnpc.todo.ProjectCreationResult;
import com.eightsnpc.todo.ProjectService;
import com.eightsnpc.todo.Task;
import com.eightsnpc.todo.TaskService;
import com.eightsnpc.todo.api.dto.ApiResponse;
import com.eightsnpc.todo.api.dto.CreateProjectRequest;
import com.eightsnpc.todo.api.dto.CreateProjectWithFirstTaskRequest;
import com.eightsnpc.todo.api.dto.ProjectResponse;
import com.eightsnpc.todo.api.dto.ProjectWithFirstTaskResponse;
import com.eightsnpc.todo.api.dto.TaskResponse;
import com.eightsnpc.todo.api.dto.UpdateProjectRequest;
import com.eightsnpc.todo.api.error.ProjectNotFoundException;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "项目管理", description = "项目的新增、查询、修改、删除和分页搜索")
public class ProjectController {
    private final ProjectService projectService;
    private final TaskService taskService;

    public ProjectController(ProjectService projectService, TaskService taskService) {
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "新增项目")
    public ResponseEntity<ApiResponse<ProjectResponse>> create(
            @Valid @RequestBody CreateProjectRequest request) {
        Project project = projectService.create(request.name(), request.description());
        URI location = URI.create("/api/v1/projects/" + project.id());
        return ResponseEntity.created(location).body(ApiResponse.success(ProjectResponse.from(project)));
    }

    @PostMapping("/with-first-task")
    @Operation(summary = "创建项目并创建首个任务（事务）")
    public ResponseEntity<ApiResponse<ProjectWithFirstTaskResponse>> createWithFirstTask(
            @Valid @RequestBody CreateProjectWithFirstTaskRequest request) {
        ProjectCreationResult result = projectService.createWithFirstTask(
                request.name(), request.description(), request.firstTaskDescription());
        URI location = URI.create("/api/v1/projects/" + result.project().id());
        ProjectWithFirstTaskResponse response = new ProjectWithFirstTaskResponse(
                ProjectResponse.from(result.project()), TaskResponse.from(result.firstTask()));
        return ResponseEntity.created(location).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询项目详情")
    public ApiResponse<ProjectResponse> getById(@PathVariable long id) {
        return projectService.findById(id)
                .map(ProjectResponse::from)
                .map(ApiResponse::success)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    @GetMapping
    @Operation(summary = "分页查询项目")
    public ApiResponse<PageResult<ProjectResponse>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        PageResult<Project> result = projectService.page(page, size, keyword);
        List<ProjectResponse> records = result.records().stream().map(ProjectResponse::from).toList();
        return ApiResponse.success(new PageResult<>(result.current(), result.size(), result.total(), records));
    }

    @GetMapping("/{projectId}/tasks")
    @Operation(summary = "查询项目下的任务")
    public ApiResponse<PageResult<TaskResponse>> listTasks(
            @PathVariable long projectId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) String keyword) {
        if (projectService.findById(projectId).isEmpty()) {
            throw new ProjectNotFoundException(projectId);
        }
        PageResult<Task> result = taskService.page(page, size, projectId, completed, keyword);
        List<TaskResponse> records = result.records().stream().map(TaskResponse::from).toList();
        return ApiResponse.success(new PageResult<>(result.current(), result.size(), result.total(), records));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改项目")
    public ApiResponse<ProjectResponse> update(
            @PathVariable long id, @Valid @RequestBody UpdateProjectRequest request) {
        return projectService.update(id, request.name(), request.description())
                .map(ProjectResponse::from)
                .map(ApiResponse::success)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除项目及其任务")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable long id) {
        if (!projectService.delete(id)) {
            throw new ProjectNotFoundException(id);
        }
        return ResponseEntity.ok(ApiResponse.successWithoutData());
    }
}
