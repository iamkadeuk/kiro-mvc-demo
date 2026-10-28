package com.example.kiromvc.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 120, message = "제목은 120자 이하여야 합니다.")
        String title,

        @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
        String description,

        boolean completed
) {
}

