package com.avaliatech.todo.todo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for updating TODO fields. Send only fields you want to modify.")
public record UpdateTodoRequest(
        @Schema(description = "Short title of the task", example = "Buy groceries and fruits")
        @Size(max = 150, message = "title length must be <= 150")
        String title,

        @Schema(description = "Detailed description", example = "Milk, eggs, bread, and apples")
        @Size(max = 500, message = "description length must be <= 500")
        String description,

        @Schema(description = "Completion status", example = "true")
        Boolean completed
) {
}
