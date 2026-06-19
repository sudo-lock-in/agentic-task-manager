package com.taskmanager.service;

import com.taskmanager.model.Task;
import com.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;

    /**
     * Create a new task
     */
    public Task createTask(Task task) {
        if (task.getStatus() == null) {
            task.setStatus(Task.Status.TODO);
        }
        if (task.getPriority() == null) {
            task.setPriority(Task.Priority.MEDIUM);
        }
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(LocalDate.now());
        }
        return taskRepository.save(task);
    }

    /**
     * Get all tasks
     */
    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Get task by ID
     */
    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    /**
     * Update an existing task
     */
    public Task updateTask(Long id, Task taskDetails) {
        return taskRepository.findById(id)
                .map(task -> {
                    if (taskDetails.getTitle() != null) {
                        task.setTitle(taskDetails.getTitle());
                    }
                    if (taskDetails.getDescription() != null) {
                        task.setDescription(taskDetails.getDescription());
                    }
                    if (taskDetails.getDueDate() != null) {
                        task.setDueDate(taskDetails.getDueDate());
                    }
                    if (taskDetails.getPriority() != null) {
                        task.setPriority(taskDetails.getPriority());
                    }
                    if (taskDetails.getStatus() != null) {
                        task.setStatus(taskDetails.getStatus());
                    }
                    return taskRepository.save(task);
                })
                .orElse(null);
    }

    /**
     * Delete a task by ID
     */
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    /**
     * Get tasks by status
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByStatus(Task.Status status) {
        return taskRepository.findByStatus(status);
    }

    /**
     * Get tasks by priority
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByPriority(Task.Priority priority) {
        return taskRepository.findByPriority(priority);
    }

    /**
     * Get tasks by date range
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByDateRange(LocalDate startDate, LocalDate endDate) {
        return taskRepository.findByDueDateBetween(startDate, endDate);
    }

    /**
     * Search tasks by title
     */
    @Transactional(readOnly = true)
    public List<Task> searchTasksByTitle(String title) {
        return taskRepository.findByTitleContainingIgnoreCase(title);
    }
}
