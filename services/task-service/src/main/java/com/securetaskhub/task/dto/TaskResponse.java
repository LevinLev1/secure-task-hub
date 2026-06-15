package com.securetaskhub.task.dto;

import com.securetaskhub.task.model.TaskStatus;
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
