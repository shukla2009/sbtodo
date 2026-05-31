package com.avaliatech.todo.todo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for creating a TODO")
public record CreateTodoRequest(
        @Schema(description = "Short title of the task", example = "Buy groceries")
        @NotBlank(message = "title is required")
        @Size(max = 150, message = "title length must be <= 150")
        String title,

        @Schema(description = "Detailed description", example = "Milk, eggs, and bread from the supermarket")
        @Size(max = 500, message = "description length must be <= 500")
        String description,

        @Schema(description = "Completion status", example = "false")
        Boolean completed
) {
}
