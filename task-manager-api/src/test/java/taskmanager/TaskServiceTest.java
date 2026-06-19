package com.taskmanager.service;

import com.taskmanager.model.Task;
import com.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .dueDate(LocalDate.now().plusDays(1))
                .priority(Task.Priority.MEDIUM)
                .status(Task.Status.TODO)
                .createdAt(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("createTask should save task with defaults")
    void testCreateTask() {
        Task newTask = Task.builder()
                .title("New Task")
                .description("New Description")
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = taskService.createTask(newTask);

        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("getAllTasks should return all tasks")
    void testGetAllTasks() {
        Task task2 = Task.builder()
                .id(2L)
                .title("Task 2")
                .status(Task.Status.DONE)
                .build();

        when(taskRepository.findAll()).thenReturn(Arrays.asList(testTask, task2));

        List<Task> result = taskService.getAllTasks();

        assertEquals(2, result.size());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getTaskById should return task when found")
    void testGetTaskById_Found() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        Optional<Task> result = taskService.getTaskById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Task", result.get().getTitle());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getTaskById should return empty when not found")
    void testGetTaskById_NotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Task> result = taskService.getTaskById(999L);

        assertFalse(result.isPresent());
        verify(taskRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("updateTask should update existing task")
    void testUpdateTask() {
        Task updates = Task.builder()
                .title("Updated Title")
                .priority(Task.Priority.HIGH)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = taskService.updateTask(1L, updates);

        assertNotNull(result);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("updateTask should return null when task not found")
    void testUpdateTask_NotFound() {
        Task updates = Task.builder().title("Updated").build();

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        Task result = taskService.updateTask(999L, updates);

        assertNull(result);
        verify(taskRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("deleteTask should remove task by id")
    void testDeleteTask() {
        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("getTasksByStatus should return filtered tasks")
    void testGetTasksByStatus() {
        List<Task> todoTasks = Arrays.asList(testTask);

        when(taskRepository.findByStatus(Task.Status.TODO)).thenReturn(todoTasks);

        List<Task> result = taskService.getTasksByStatus(Task.Status.TODO);

        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByStatus(Task.Status.TODO);
    }

    @Test
    @DisplayName("getTasksByPriority should return filtered tasks")
    void testGetTasksByPriority() {
        List<Task> highPriorityTasks = Arrays.asList(testTask);

        when(taskRepository.findByPriority(Task.Priority.MEDIUM)).thenReturn(highPriorityTasks);

        List<Task> result = taskService.getTasksByPriority(Task.Priority.MEDIUM);

        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByPriority(Task.Priority.MEDIUM);
    }

    @Test
    @DisplayName("searchTasksByTitle should return matching tasks")
    void testSearchTasksByTitle() {
        List<Task> matchingTasks = Arrays.asList(testTask);

        when(taskRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(matchingTasks);

        List<Task> result = taskService.searchTasksByTitle("Test");

        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByTitleContainingIgnoreCase("Test");
    }
}
