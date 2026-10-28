package com.example.kiromvc.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 120, message = "제목은 120자 이하여야 합니다.")
        String title,

        @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
        String description,

        boolean completed,

        Priority priority,           // nullable — null이면 서비스 레이어에서 기본값 처리

        @JsonFormat(pattern = "yyyy-MM-dd")
        java.time.LocalDate dueDate  // nullable — 선택 입력
) {
}

