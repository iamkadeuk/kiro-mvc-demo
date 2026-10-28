package com.example.kiromvc.task;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long id) {
        super("할 일을 찾을 수 없습니다. id=" + id);
    }
}
