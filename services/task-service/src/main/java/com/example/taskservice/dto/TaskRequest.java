package com.example.taskservice.dto;

import com.example.taskservice.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 500) String description,
        @NotNull TaskStatus status
) {
}
