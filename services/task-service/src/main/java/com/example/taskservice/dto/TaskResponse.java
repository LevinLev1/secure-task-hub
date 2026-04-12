package com.example.taskservice.dto;

import com.example.taskservice.model.TaskStatus;
import java.time.Instant;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        String ownerUsername,
        Instant createdAt,
        Instant updatedAt
) {
}
