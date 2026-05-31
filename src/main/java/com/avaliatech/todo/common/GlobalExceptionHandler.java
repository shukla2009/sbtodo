package com.avaliatech.todo.common;

import com.avaliatech.todo.todo.TodoNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TodoNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTodoNotFound(TodoNotFoundException ex) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Resource not found",
                List.of(ex.getMessage()),
                OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                errors,
                OffsetDateTime.now()
        );
        return ResponseEntity.badRequest().body(response);
    }
}
