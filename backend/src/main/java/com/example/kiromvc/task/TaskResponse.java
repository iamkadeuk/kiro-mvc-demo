package com.example.kiromvc.task;

import java.time.Instant;

public record TaskResponse(
        Long id,
        String title,
        String description,
        boolean completed,
        Priority priority,
        java.time.LocalDate dueDate,
        boolean overdue,
        Instant createdAt,
        Instant updatedAt
) {
    static TaskResponse from(TaskItem task) {
        boolean overdue = !task.isCompleted()
                && task.getDueDate() != null
                && task.getDueDate().isBefore(java.time.LocalDate.now());

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getPriority(),
                task.getDueDate(),
                overdue,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}

