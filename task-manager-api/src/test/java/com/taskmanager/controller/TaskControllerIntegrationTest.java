package com.taskmanager.controller;

import com.taskmanager.model.Task;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.service.OllamaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("TaskController Integration Tests")
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @MockBean
    @SuppressWarnings("unused")
    private OllamaService ollamaService;

    private TaskRequest taskRequest;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();

        taskRequest = TaskRequest.builder()
                .title("New Task")
                .description("New Description")
                .dueDate(LocalDate.now().plusDays(1))
                .priority(Task.Priority.HIGH)
                .status(Task.Status.TODO)
                .build();
    }

    private Task seedTask(String title, Task.Priority priority, Task.Status status) {
        return taskRepository.save(Task.builder()
                .title(title)
                .description(title + " Description")
                .dueDate(LocalDate.now().plusDays(1))
                .priority(priority)
                .status(status)
                .build());
    }

    @Test
    @DisplayName("POST /tasks should create task and return 201")
    void testCreateTask() throws Exception {
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        assertEquals(1L, taskRepository.count());
    }

    @Test
    @DisplayName("POST /tasks without title should return 400")
    void testCreateTask_MissingTitle() throws Exception {
        TaskRequest invalidRequest = TaskRequest.builder()
                .description("Missing title")
                .build();

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("title is required"));
    }

    @Test
    @DisplayName("GET /tasks should return all tasks")
    void testGetAllTasks() throws Exception {
        Task task1 = seedTask("Task 1", Task.Priority.MEDIUM, Task.Status.TODO);
        Task task2 = seedTask("Task 2", Task.Priority.HIGH, Task.Status.DONE);

        mockMvc.perform(get("/tasks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(task1.getId()))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].id").value(task2.getId()))
                .andExpect(jsonPath("$[1].title").value("Task 2"));
    }

    @Test
    @DisplayName("GET /tasks/{id} should return task")
    void testGetTaskById() throws Exception {
        Task saved = seedTask("Task 1", Task.Priority.MEDIUM, Task.Status.TODO);

        mockMvc.perform(get("/tasks/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Task 1"));
    }

    @Test
    @DisplayName("GET /tasks/{id} should return 404 when not found")
    void testGetTaskById_NotFound() throws Exception {
        mockMvc.perform(get("/tasks/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /tasks/{id} should update task")
    void testUpdateTask() throws Exception {
        Task saved = seedTask("Original Task", Task.Priority.MEDIUM, Task.Status.TODO);

        TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Task")
                .description("Updated Description")
                .dueDate(LocalDate.now().plusDays(7))
                .priority(Task.Priority.HIGH)
                .status(Task.Status.DONE)
                .build();

        mockMvc.perform(put("/tasks/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("DONE"));

        Optional<Task> updated = taskRepository.findById(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated Task", updated.get().getTitle());
    }

    @Test
    @DisplayName("PUT /tasks/{id} should return 404 when not found")
    void testUpdateTask_NotFound() throws Exception {
        mockMvc.perform(put("/tasks/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /tasks/{id} should remove task and return 204")
    void testDeleteTask() throws Exception {
        Task saved = seedTask("Delete Me", Task.Priority.LOW, Task.Status.TODO);

        mockMvc.perform(delete("/tasks/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertTrue(taskRepository.findById(saved.getId()).isEmpty());
    }

    @Test
    @DisplayName("POST /tasks/suggest should return AI suggestion")
    void testSuggestTask() throws Exception {
        when(ollamaService.suggestTaskFromDescription(anyString())).thenReturn(Task.builder()
                .title("Finish project report")
                .description("Create charts and analysis for project report")
                .priority(Task.Priority.HIGH)
                .status(Task.Status.TODO)
                .build());

        SuggestTaskRequest request = SuggestTaskRequest.builder()
                .description("I need to finish the project report by next Friday with charts and analysis")
                .build();

        mockMvc.perform(post("/tasks/suggest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Finish project report"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));

        verify(ollamaService, times(1)).suggestTaskFromDescription(anyString());
    }
}
