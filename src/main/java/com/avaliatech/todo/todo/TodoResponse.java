package com.avaliatech.todo.todo;

import java.time.OffsetDateTime;

public record TodoResponse(
        Long id,
        String title,
        String description,
        boolean completed,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    static TodoResponse from(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
        );
    }
}
