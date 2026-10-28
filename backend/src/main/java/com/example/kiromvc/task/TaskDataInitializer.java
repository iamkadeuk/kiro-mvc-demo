package com.example.kiromvc.task;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TaskDataInitializer {

    @Bean
    CommandLineRunner seedTasks(TaskRepository taskRepository) {
        return args -> {
            if (taskRepository.count() > 0) {
                return;
            }

            taskRepository.saveAll(List.of(
                    new TaskItem("Kiro 프로젝트 열기", "README를 읽고 프로젝트 구조를 확인합니다.", true),
                    new TaskItem("MVC 계층 살펴보기", "Controller → Service → Repository 흐름을 따라가 봅니다.", false),
                    new TaskItem("새 기능 요청해 보기", "Kiro에게 검색 또는 마감일 기능을 추가해 달라고 요청합니다.", false)
            ));
        };
    }
}

