package com.avaliatech.todo.todo;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<TodoResponse> findAll() {
        return todoRepository.findAll().stream()
                .map(TodoResponse::from)
                .toList();
    }

    public TodoResponse findById(Long id) {
        return TodoResponse.from(getTodoOrThrow(id));
    }

    public TodoResponse create(TodoRequest request) {
        Todo todo = new Todo();
        todo.setTitle(request.title());
        todo.setDescription(request.description());
        todo.setCompleted(request.completed());
        return TodoResponse.from(todoRepository.save(todo));
    }

    public TodoResponse update(Long id, TodoRequest request) {
        Todo todo = getTodoOrThrow(id);
        todo.setTitle(request.title());
        todo.setDescription(request.description());
        todo.setCompleted(request.completed());
        return TodoResponse.from(todoRepository.save(todo));
    }

    public void delete(Long id) {
        Todo todo = getTodoOrThrow(id);
        todoRepository.delete(todo);
    }

    private Todo getTodoOrThrow(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }
}
