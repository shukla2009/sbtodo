package com.avaliatech.todo.todo;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public List<TodoResponse> getAll() {
        return todoService.findAll();
    }

    @GetMapping("/{id}")
    public TodoResponse getById(@PathVariable String id) {
        return todoService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create a new TODO")
    public ResponseEntity<TodoResponse> create(
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Create TODO payload",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "Create example",
                                    value = """
                                            {
                                              "title": "Learn Spring Boot",
                                              "description": "Implement TODO CRUD API",
                                              "completed": false
                                            }
                                            """
                            )
                    )
            )
            CreateTodoRequest request
    ) {
        TodoResponse response = todoService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update one or more TODO fields")
    public TodoResponse update(
            @PathVariable String id,
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Update TODO payload. Include only fields you want to change.",
                    required = true,
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "Update title only",
                                            value = """
                                                    {
                                                      "title": "Updated title only"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Mark completed",
                                            value = """
                                                    {
                                                      "completed": true
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Update description only",
                                            value = """
                                                    {
                                                      "description": "Updated details"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            UpdateTodoRequest request
    ) {
        return todoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
