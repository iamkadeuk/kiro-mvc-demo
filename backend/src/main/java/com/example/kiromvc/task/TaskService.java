package com.example.kiromvc.task;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskResponse> findAll() {
        return taskRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse findById(Long id) {
        return TaskResponse.from(getTask(id));
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Priority priority = Optional.ofNullable(request.priority())
                .orElse(Priority.MEDIUM);
        TaskItem task = new TaskItem(
                request.title().trim(),
                normalizeDescription(request.description()),
                request.completed(),
                priority,
                request.dueDate()
        );
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        TaskItem task = getTask(id);
        Priority priority = Optional.ofNullable(request.priority())
                .orElse(task.getPriority()); // null이면 기존 값 유지
        task.update(
                request.title().trim(),
                normalizeDescription(request.description()),
                request.completed(),
                priority,
                request.dueDate()
        );
        return TaskResponse.from(task);
    }

    @Transactional
    public void delete(Long id) {
        taskRepository.delete(getTask(id));
    }

    private TaskItem getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }
}

