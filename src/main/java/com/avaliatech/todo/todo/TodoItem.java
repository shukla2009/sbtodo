package com.avaliatech.todo.todo;

import java.time.OffsetDateTime;

public record TodoItem(
        String id,
        String title,
        String description,
        boolean completed,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
