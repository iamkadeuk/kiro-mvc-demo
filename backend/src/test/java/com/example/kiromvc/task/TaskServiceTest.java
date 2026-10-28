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

    // ─── 기존 동작 유지 ───────────────────────────────────────────────────────

    @Test
    void createTrimsInputAndSavesTask() {
        when(taskRepository.save(any(TaskItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse result = taskService.create(
                new TaskRequest("  MVC 공부  ", "  계층별 역할 확인  ", false, null, null)
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

    // ─── priority 기본값 및 유지 ──────────────────────────────────────────────

    @Test
    void createWithNullPriorityDefaultsToMedium() {
        when(taskRepository.save(any(TaskItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        TaskResponse result = taskService.create(
                new TaskRequest("테스트", null, false, null, null)
        );
        assertThat(result.priority()).isEqualTo(Priority.MEDIUM);
    }

    @Test
    void updateWithNullPriorityPreservesExistingPriority() {
        TaskItem existing = new TaskItem("기존", null, false, Priority.LOW, null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        TaskResponse result = taskService.update(1L,
                new TaskRequest("기존", null, false, null, null));
        assertThat(result.priority()).isEqualTo(Priority.LOW);
    }

    // ─── overdue 계산 ─────────────────────────────────────────────────────────

    @Test
    void overdueIsTrueForPastDueDateAndNotCompleted() {
        TaskItem task = new TaskItem("t", null, false, Priority.MEDIUM,
                java.time.LocalDate.now().minusDays(1));
        var response = TaskResponse.from(task);
        assertThat(response.overdue()).isTrue();
    }

    @Test
    void overdueIsFalseWhenCompleted() {
        TaskItem task = new TaskItem("t", null, true, Priority.MEDIUM,
                java.time.LocalDate.now().minusDays(1));
        assertThat(TaskResponse.from(task).overdue()).isFalse();
    }

    @Test
    void overdueIsFalseWhenDueDateIsNull() {
        TaskItem task = new TaskItem("t", null, false, Priority.MEDIUM, null);
        assertThat(TaskResponse.from(task).overdue()).isFalse();
    }

    @Test
    void overdueIsFalseWhenDueDateIsToday() {
        TaskItem task = new TaskItem("t", null, false, Priority.MEDIUM,
                java.time.LocalDate.now());
        assertThat(TaskResponse.from(task).overdue()).isFalse();
    }

    @Test
    void overdueIsFalseWhenDueDateIsFuture() {
        TaskItem task = new TaskItem("t", null, false, Priority.MEDIUM,
                java.time.LocalDate.now().plusDays(1));
        assertThat(TaskResponse.from(task).overdue()).isFalse();
    }
}
