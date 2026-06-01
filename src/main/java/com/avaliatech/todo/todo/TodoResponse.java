package com.avaliatech.todo.todo;

import java.time.OffsetDateTime;

public record TodoResponse(
        String id,
        String title,
        String description,
        boolean completed,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    static TodoResponse from(TodoItem todo) {
        return new TodoResponse(
                todo.id(),
                todo.title(),
                todo.description(),
                todo.completed(),
                todo.createdAt(),
                todo.updatedAt()
        );
    }
}
