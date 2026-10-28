package com.example.kiromvc.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTrimsInputAndSavesTask() {
        when(taskRepository.save(any(TaskItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse result = taskService.create(
                new TaskRequest("  MVC 공부  ", "  계층별 역할 확인  ", false)
        );

        assertThat(result.title()).isEqualTo("MVC 공부");
        assertThat(result.description()).isEqualTo("계층별 역할 확인");
        verify(taskRepository).save(any(TaskItem.class));
    }

    @Test
    void findByIdThrowsWhenTaskDoesNotExist() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }
}

