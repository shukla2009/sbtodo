package com.avaliatech.todo.todo.store;

import com.avaliatech.todo.todo.CreateTodoRequest;
import com.avaliatech.todo.todo.TodoItem;
import com.avaliatech.todo.todo.UpdateTodoRequest;
import java.util.List;
import java.util.Optional;

public interface TodoStore {
    List<TodoItem> findAll();

    Optional<TodoItem> findById(String id);

    TodoItem create(CreateTodoRequest request);

    TodoItem update(String id, UpdateTodoRequest request);

    void delete(String id);
}
