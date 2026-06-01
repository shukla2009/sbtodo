package com.avaliatech.todo.todo;

import java.util.List;
import org.springframework.stereotype.Service;
import com.avaliatech.todo.todo.store.TodoStore;

@Service
public class TodoService {

    private final TodoStore todoStore;

    public TodoService(TodoStore todoStore) {
        this.todoStore = todoStore;
    }

    public List<TodoResponse> findAll() {
        return todoStore.findAll().stream()
                .map(TodoResponse::from)
                .toList();
    }

    public TodoResponse findById(String id) {
        return TodoResponse.from(getTodoOrThrow(id));
    }

    public TodoResponse create(CreateTodoRequest request) {
        return TodoResponse.from(todoStore.create(request));
    }

    public TodoResponse update(String id, UpdateTodoRequest request) {
        return TodoResponse.from(todoStore.update(id, request));
    }

    public void delete(String id) {
        todoStore.delete(id);
    }

    private TodoItem getTodoOrThrow(String id) {
        return todoStore.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }
}
