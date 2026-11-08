package com.eightsnpc.todo.api;

import com.eightsnpc.todo.Comment;
import com.eightsnpc.todo.CommentService;
import com.eightsnpc.todo.PageResult;
import com.eightsnpc.todo.api.dto.ApiResponse;
import com.eightsnpc.todo.api.dto.CommentResponse;
import com.eightsnpc.todo.api.dto.CreateCommentRequest;
import com.eightsnpc.todo.api.dto.UpdateCommentRequest;
import com.eightsnpc.todo.api.error.CommentNotFoundException;
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
@RequestMapping("/api/v1")
@Tag(name = "评论管理", description = "任务评论的新增、查询、修改和删除")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/tasks/{taskId}/comments")
    @Operation(summary = "为任务新增评论")
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @PathVariable long taskId, @Valid @RequestBody CreateCommentRequest request) {
        Comment comment = commentService.create(taskId, request.content());
        URI location = URI.create("/api/v1/comments/" + comment.id());
        return ResponseEntity.created(location).body(ApiResponse.success(CommentResponse.from(comment)));
    }

    @GetMapping("/tasks/{taskId}/comments")
    @Operation(summary = "查询任务评论")
    public ApiResponse<PageResult<CommentResponse>> listByTask(
            @PathVariable long taskId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        PageResult<Comment> result = commentService.pageByTask(taskId, page, size);
        List<CommentResponse> records = result.records().stream().map(CommentResponse::from).toList();
        return ApiResponse.success(new PageResult<>(result.current(), result.size(), result.total(), records));
    }

    @GetMapping("/comments/{id}")
    @Operation(summary = "查询评论详情")
    public ApiResponse<CommentResponse> getById(@PathVariable long id) {
        return commentService.findById(id)
                .map(CommentResponse::from)
                .map(ApiResponse::success)
                .orElseThrow(() -> new CommentNotFoundException(id));
    }

    @PutMapping("/comments/{id}")
    @Operation(summary = "修改评论")
    public ApiResponse<CommentResponse> update(
            @PathVariable long id, @Valid @RequestBody UpdateCommentRequest request) {
        return commentService.update(id, request.content())
                .map(CommentResponse::from)
                .map(ApiResponse::success)
                .orElseThrow(() -> new CommentNotFoundException(id));
    }

    @DeleteMapping("/comments/{id}")
    @Operation(summary = "删除评论")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable long id) {
        if (!commentService.delete(id)) {
            throw new CommentNotFoundException(id);
        }
        return ResponseEntity.ok(ApiResponse.successWithoutData());
    }
}
