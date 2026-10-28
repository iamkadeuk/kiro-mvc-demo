package com.example.kiromvc.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskItem, Long> {

    List<TaskItem> findAllByOrderByCreatedAtDesc();
}

