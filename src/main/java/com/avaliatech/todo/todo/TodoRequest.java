package com.avaliatech.todo.todo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TodoRequest(
        @NotBlank(message = "title is required")
        @Size(max = 150, message = "title length must be <= 150")
        String title,

        @Size(max = 500, message = "description length must be <= 500")
        String description,

        boolean completed
) {
}
