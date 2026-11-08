package com.eightsnpc.todo.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.matchesPattern;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.eightsnpc.todo.CommentService;
import com.eightsnpc.todo.ProjectService;
import com.eightsnpc.todo.TaskService;
import com.eightsnpc.todo.TaskWorkflowService;
import com.eightsnpc.todo.persistence.mapper.CommentMapper;
import com.eightsnpc.todo.persistence.mapper.ProjectMapper;
import com.eightsnpc.todo.persistence.mapper.TaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private TaskWorkflowService taskWorkflowService;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private CommentMapper commentMapper;

    @BeforeEach
    void clearDatabase() {
        commentMapper.delete(new QueryWrapper<>());
        taskMapper.delete(new QueryWrapper<>());
        projectMapper.delete(new QueryWrapper<>());
    }

    @Test
    void healthReturnsUnifiedSuccessResponse() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void createTaskReturnsCreatedTaskAndLocation() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"学习 MySQL\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/tasks/\\d+")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.description").value("学习 MySQL"))
                .andExpect(jsonPath("$.data.projectId").doesNotExist())
                .andExpect(jsonPath("$.data.completed").value(false));
    }

    @Test
    void listTasksReturnsFilteredPage() throws Exception {
        taskService.add("MySQL 分页练习");
        taskService.add("Java 复习");

        mockMvc.perform(get("/api/v1/tasks")
                        .param("page", "1")
                        .param("size", "1")
                        .param("keyword", "MySQL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].description").value("MySQL 分页练习"));
    }

    @Test
    void getTaskByIdReturnsDetails() throws Exception {
        var task = taskService.add("详情测试任务");

        mockMvc.perform(get("/api/v1/tasks/{id}", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(task.getId()))
                .andExpect(jsonPath("$.data.description").value("详情测试任务"));
    }

    @Test
    void updateTaskChangesDescriptionAndStatus() throws Exception {
        var task = taskService.add("待修改任务");

        mockMvc.perform(put("/api/v1/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"已修改任务\",\"completed\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.description").value("已修改任务"))
                .andExpect(jsonPath("$.data.completed").value(true));
    }

    @Test
    void deleteTaskRemovesItAndReturnsUnifiedResponse() throws Exception {
        var task = taskService.add("待删除任务");

        mockMvc.perform(delete("/api/v1/tasks/{id}", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist());
        assertEquals(0, taskService.page(1, 10, null, null, null).total());
    }

    @Test
    void blankTaskDescriptionReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }

    @Test
    void missingTaskReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TASK_NOT_FOUND"));
    }

    @Test
    void createProjectReturnsPersistedProject() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"MySQL 学习\",\"description\":\"数据库练习\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("MySQL 学习"));
    }

    @Test
    void listProjectsSupportsPaginationAndNameSearch() throws Exception {
        projectService.create("Java 项目", null);
        projectService.create("Java 数据库练习", "MySQL");
        projectService.create("Spring 项目", null);

        mockMvc.perform(get("/api/v1/projects")
                        .param("page", "1")
                        .param("size", "1")
                        .param("keyword", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records.length()").value(1));
    }

    @Test
    void getAndUpdateProjectWork() throws Exception {
        var project = projectService.create("旧项目名", "旧描述");

        mockMvc.perform(put("/api/v1/projects/{id}", project.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"新项目名\",\"description\":\"新描述\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("新项目名"));
        mockMvc.perform(get("/api/v1/projects/{id}", project.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("新描述"));
    }

    @Test
    void deleteProjectAlsoDeletesItsTasks() throws Exception {
        var project = projectService.create("级联删除项目", null);
        taskService.add("项目任务", project.id());

        mockMvc.perform(delete("/api/v1/projects/{id}", project.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        assertEquals(0, taskService.page(1, 10, null, null, null).total());
    }

    @Test
    void createProjectWithFirstTaskPersistsBoth() throws Exception {
        mockMvc.perform(post("/api/v1/projects/with-first-task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"事务项目\",\"description\":\"回归测试\","
                                + "\"firstTaskDescription\":\"首个任务\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.project.name").value("事务项目"))
                .andExpect(jsonPath("$.data.firstTask.description").value("首个任务"))
                .andExpect(jsonPath("$.data.firstTask.projectId").isNotEmpty());
        assertEquals(1, taskService.page(1, 10, null, null, null).total());
    }

    @Test
    void failedFirstTaskCreationRollsBackProjectInsert() {
        assertThrows(IllegalArgumentException.class,
                () -> projectService.createWithFirstTask("回滚项目", null, "   "));
        assertEquals(0, projectService.page(1, 10, null).total());
    }

    @Test
    void projectNotFoundReturnsUnifiedError() throws Exception {
        mockMvc.perform(get("/api/v1/projects/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PROJECT_NOT_FOUND"));
    }

    @Test
    void createCommentReturnsCreatedComment() throws Exception {
        var task = taskService.add("带评论的任务");

        mockMvc.perform(post("/api/v1/tasks/{taskId}/comments", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"已开始处理\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/comments/\\d+")))
                .andExpect(jsonPath("$.data.taskId").value(task.getId()))
                .andExpect(jsonPath("$.data.content").value("已开始处理"))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty());
    }

    @Test
    void listCommentsByTaskSupportsPaginationAndStableOrdering() throws Exception {
        var task = taskService.add("评论分页任务");
        commentService.create(task.getId(), "第一条评论");
        var newest = commentService.create(task.getId(), "第二条评论");

        mockMvc.perform(get("/api/v1/tasks/{taskId}/comments", task.getId())
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(newest.id()));
    }

    @Test
    void getAndUpdateCommentWork() throws Exception {
        var task = taskService.add("评论详情任务");
        var comment = commentService.create(task.getId(), "原评论");

        mockMvc.perform(put("/api/v1/comments/{id}", comment.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"修改后的评论\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("修改后的评论"));
        mockMvc.perform(get("/api/v1/comments/{id}", comment.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(task.getId()))
                .andExpect(jsonPath("$.data.content").value("修改后的评论"));
    }

    @Test
    void deleteCommentRemovesIt() throws Exception {
        var task = taskService.add("评论删除任务");
        var comment = commentService.create(task.getId(), "待删除评论");

        mockMvc.perform(delete("/api/v1/comments/{id}", comment.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        assertEquals(0, commentService.pageByTask(task.getId(), 1, 10).total());
    }

    @Test
    void blankCommentReturnsBadRequest() throws Exception {
        var task = taskService.add("评论校验任务");

        mockMvc.perform(post("/api/v1/tasks/{taskId}/comments", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
    }

    @Test
    void missingCommentReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/comments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("COMMENT_NOT_FOUND"));
    }

    @Test
    void commentsForMissingTaskReturnTaskNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/999/comments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("TASK_NOT_FOUND"));
    }

    @Test
    void projectsExposeTheirTasksThroughAssociatedQuery() throws Exception {
        var project = projectService.create("关联查询项目", null);
        taskService.add("关联项目任务", project.id());
        taskService.add("独立任务");

        mockMvc.perform(get("/api/v1/projects/{projectId}/tasks", project.id())
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].projectId").value(project.id()));
    }

    @Test
    void taskStatusCanBeCompletedAndReopened() throws Exception {
        var task = taskService.add("状态流转任务");

        mockMvc.perform(patch("/api/v1/tasks/{id}/status", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true));
        mockMvc.perform(patch("/api/v1/tasks/{id}/status", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(false));
    }

    @Test
    void missingTaskStatusUpdateReturnsNotFound() throws Exception {
        mockMvc.perform(patch("/api/v1/tasks/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\":true}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("TASK_NOT_FOUND"));
    }

    @Test
    void createTaskWithFirstCommentPersistsBoth() throws Exception {
        mockMvc.perform(post("/api/v1/tasks/with-first-comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"事务任务\","
                                + "\"firstCommentContent\":\"首条进度\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.task.description").value("事务任务"))
                .andExpect(jsonPath("$.data.firstComment.content").value("首条进度"))
                .andExpect(jsonPath("$.data.firstComment.taskId").isNotEmpty());
        assertEquals(1, taskService.page(1, 10, null, null, null).total());
    }

    @Test
    void failedFirstCommentCreationRollsBackTaskInsert() {
        assertThrows(IllegalArgumentException.class,
                () -> taskWorkflowService.createWithFirstComment("回滚任务", null, "   "));
        assertEquals(0, taskService.page(1, 10, null, null, null).total());
        assertEquals(0, commentMapper.selectCount(new QueryWrapper<>()));
    }

    @Test
    void deletingTaskAlsoDeletesItsComments() throws Exception {
        var task = taskService.add("评论级联任务");
        commentService.create(task.getId(), "将随任务删除");

        mockMvc.perform(delete("/api/v1/tasks/{id}", task.getId()))
                .andExpect(status().isOk());
        assertEquals(0, commentMapper.selectCount(new QueryWrapper<>()));
    }
}
