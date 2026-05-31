package com.avaliatech.todo;

import com.avaliatech.todo.todo.Todo;
import com.avaliatech.todo.todo.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class TodoApplicationTests {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private TodoRepository todoRepository;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		todoRepository.deleteAll();
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void shouldCreateTodo() throws Exception {
		String request = """
				{
				  "title": "Learn Spring Boot",
				  "description": "Implement CRUD",
				  "completed": false
				}
				""";

		mockMvc.perform(post("/api/v1/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.title").value("Learn Spring Boot"))
				.andExpect(jsonPath("$.completed").value(false));
	}

	@Test
	void shouldGetAllTodos() throws Exception {
		Todo todo = new Todo();
		todo.setTitle("First");
		todo.setDescription("First todo");
		todo.setCompleted(false);
		todoRepository.save(todo);

		mockMvc.perform(get("/api/v1/todos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].title").value("First"));
	}

	@Test
	void shouldUpdateTodo() throws Exception {
		Todo todo = new Todo();
		todo.setTitle("Old title");
		todo.setDescription("Old description");
		todo.setCompleted(false);
		Todo saved = todoRepository.save(todo);

		String request = """
				{
				  "title": "New title",
				  "description": "New description",
				  "completed": true
				}
				""";

		mockMvc.perform(put("/api/v1/todos/{id}", saved.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(saved.getId()))
				.andExpect(jsonPath("$.title").value("New title"))
				.andExpect(jsonPath("$.completed").value(true));
	}

	@Test
	void shouldDeleteTodo() throws Exception {
		Todo todo = new Todo();
		todo.setTitle("Delete me");
		todo.setDescription("To be removed");
		todo.setCompleted(false);
		Todo saved = todoRepository.save(todo);

		mockMvc.perform(delete("/api/v1/todos/{id}", saved.getId()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/todos/{id}", saved.getId()))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
		String request = """
				{
				  "title": "",
				  "description": "Invalid",
				  "completed": false
				}
				""";

		mockMvc.perform(post("/api/v1/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Validation failed"));
	}

	@Test
	void shouldExposeOpenApiSpec() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openapi").exists())
				.andExpect(jsonPath("$.info.title").value("Todo API"));
	}

	@Test
	void shouldExposeSwaggerUi() throws Exception {
		mockMvc.perform(get("/swagger-ui.html"))
				.andExpect(status().is3xxRedirection());
	}
}
