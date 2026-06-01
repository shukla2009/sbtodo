package com.avaliatech.todo.todo;

public class TodoNotFoundException extends RuntimeException {
    public TodoNotFoundException(String id) {
        super("Todo not found with id: " + id);
    }
}
