package com.avaliatech.todo.todo.store.jpa;

import com.avaliatech.todo.todo.CreateTodoRequest;
import com.avaliatech.todo.todo.Todo;
import com.avaliatech.todo.todo.TodoItem;
import com.avaliatech.todo.todo.TodoNotFoundException;
import com.avaliatech.todo.todo.TodoRepository;
import com.avaliatech.todo.todo.UpdateTodoRequest;
import com.avaliatech.todo.todo.store.TodoStore;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"h2", "postgres", "oracle"})
public class JpaTodoStore implements TodoStore {

    private final TodoRepository todoRepository;

    public JpaTodoStore(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Override
    public List<TodoItem> findAll() {
        return todoRepository.findAll().stream()
                .map(this::toItem)
                .toList();
    }

    @Override
    public Optional<TodoItem> findById(String id) {
        try {
            long parsedId = Long.parseLong(id);
            return todoRepository.findById(parsedId).map(this::toItem);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    @Override
    public TodoItem create(CreateTodoRequest request) {
        Todo todo = new Todo();
        todo.setTitle(request.title());
        todo.setDescription(request.description());
        todo.setCompleted(Boolean.TRUE.equals(request.completed()));
        return toItem(todoRepository.save(todo));
    }

    @Override
    public TodoItem update(String id, UpdateTodoRequest request) {
        long parsedId = parseIdOrThrow(id);
        Todo todo = todoRepository.findById(parsedId).orElseThrow(() -> new TodoNotFoundException(id));
        if (request.title() != null) {
            todo.setTitle(request.title());
        }
        if (request.description() != null) {
            todo.setDescription(request.description());
        }
        if (request.completed() != null) {
            todo.setCompleted(request.completed());
        }
        return toItem(todoRepository.save(todo));
    }

    @Override
    public void delete(String id) {
        long parsedId = parseIdOrThrow(id);
        Todo todo = todoRepository.findById(parsedId).orElseThrow(() -> new TodoNotFoundException(id));
        todoRepository.delete(todo);
    }

    private long parseIdOrThrow(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new TodoNotFoundException(id);
        }
    }

    private TodoItem toItem(Todo todo) {
        return new TodoItem(
                String.valueOf(todo.getId()),
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
        );
    }
}
