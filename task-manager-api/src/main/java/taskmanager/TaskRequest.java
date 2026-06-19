package com.taskmanager.controller;

import com.taskmanager.model.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequest {

    @NotBlank(message = "title is required")
    private String title;

    private String description;

    private LocalDate dueDate;

    private Task.Priority priority;

    private Task.Status status;
}
