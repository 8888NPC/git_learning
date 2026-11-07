package com.eightsnpc.todo.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.matchesPattern;

import com.eightsnpc.todo.Task;
import com.eightsnpc.todo.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TaskApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;

    @BeforeEach
    void clearTasks() {
        taskService.list().forEach(task -> taskService.delete(task.getId()));
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
    void createReturnsCreatedTaskAndLocation() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"学习 MockMvc\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/tasks/\\d+")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.description").value("学习 MockMvc"))
                .andExpect(jsonPath("$.data.completed").value(false));
    }

    @Test
    void listReturnsTasksInResponseData() throws Exception {
        taskService.add("列表测试任务");

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].description").value("列表测试任务"));
    }

    @Test
    void getByIdReturnsTaskDetails() throws Exception {
        Task task = taskService.add("详情测试任务");

        mockMvc.perform(get("/api/v1/tasks/{id}", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(task.getId()))
                .andExpect(jsonPath("$.data.description").value("详情测试任务"));
    }

    @Test
    void updateChangesTaskFields() throws Exception {
        Task task = taskService.add("待修改任务");

        mockMvc.perform(put("/api/v1/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"已修改任务\",\"completed\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.description").value("已修改任务"))
                .andExpect(jsonPath("$.data.completed").value(true));
    }

    @Test
    void deleteRemovesTaskAndReturnsUnifiedSuccessResponse() throws Exception {
        Task task = taskService.add("待删除任务");

        mockMvc.perform(delete("/api/v1/tasks/{id}", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error").doesNotExist());

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void blankDescriptionReturnsUnifiedBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error.message").value("任务内容不能为空"));
    }

    @Test
    void missingCompletedFieldReturnsUnifiedBadRequest() throws Exception {
        Task task = taskService.add("参数校验任务");

        mockMvc.perform(put("/api/v1/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"缺少 completed\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error.message").value("completed 字段不能为空"));
    }

    @Test
    void missingTaskReturnsUnifiedNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TASK_NOT_FOUND"));
    }
}
