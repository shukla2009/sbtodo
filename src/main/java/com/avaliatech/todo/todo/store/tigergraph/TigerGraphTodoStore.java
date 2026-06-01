package com.avaliatech.todo.todo.store.tigergraph;

import com.avaliatech.todo.todo.CreateTodoRequest;
import com.avaliatech.todo.todo.TodoItem;
import com.avaliatech.todo.todo.TodoNotFoundException;
import com.avaliatech.todo.todo.UpdateTodoRequest;
import com.avaliatech.todo.todo.store.TodoStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@Profile("tigergraph")
public class TigerGraphTodoStore implements TodoStore {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String graphName;
    private final String vertexType;
    private final String authToken;

    public TigerGraphTodoStore(
            @Value("${tigergraph.base-url}") String baseUrl,
            @Value("${tigergraph.graph-name}") String graphName,
            @Value("${tigergraph.vertex-type}") String vertexType,
            @Value("${tigergraph.auth-token:}") String authToken
    ) {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
        this.baseUrl = baseUrl;
        this.graphName = graphName;
        this.vertexType = vertexType;
        this.authToken = authToken == null ? "" : authToken.trim();
    }

    @Override
    public List<TodoItem> findAll() {
        HttpRequest request = requestBuilder(graphPath("/vertices/" + vertexType)).GET().build();
        JsonNode response = execute(request, "Failed to list TODOs from TigerGraph");
        JsonNode results = response.path("results");
        List<TodoItem> items = new ArrayList<>();
        if (results.isArray()) {
            for (JsonNode node : results) {
                items.add(toItem(node));
            }
        }
        return items;
    }

    @Override
    public Optional<TodoItem> findById(String id) {
        HttpRequest request = requestBuilder(graphPath("/vertices/" + vertexType + "/" + encode(id))).GET().build();
        JsonNode response = execute(request, "Failed to fetch TODO from TigerGraph");
        JsonNode results = response.path("results");
        if (!results.isArray() || results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toItem(results.get(0)));
    }

    @Override
    public TodoItem create(CreateTodoRequest request) {
        String id = UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.now();
        TodoItem item = new TodoItem(
                id,
                request.title(),
                request.description(),
                Boolean.TRUE.equals(request.completed()),
                now,
                now
        );
        upsert(item);
        return item;
    }

    @Override
    public TodoItem update(String id, UpdateTodoRequest request) {
        TodoItem existing = findById(id).orElseThrow(() -> new TodoNotFoundException(id));
        TodoItem updated = new TodoItem(
                existing.id(),
                request.title() != null ? request.title() : existing.title(),
                request.description() != null ? request.description() : existing.description(),
                request.completed() != null ? request.completed() : existing.completed(),
                existing.createdAt(),
                OffsetDateTime.now()
        );
        upsert(updated);
        return updated;
    }

    @Override
    public void delete(String id) {
        if (findById(id).isEmpty()) {
            throw new TodoNotFoundException(id);
        }

        HttpRequest request = requestBuilder(graphPath("/vertices/" + vertexType + "/" + encode(id)))
                .DELETE()
                .build();
        execute(request, "Failed to delete TODO from TigerGraph");
    }

    private void upsert(TodoItem item) {
        String payload = """
                {
                  "vertices": {
                    "%s": {
                      "%s": {
                        "title": { "value": %s },
                        "description": { "value": %s },
                        "completed": { "value": %s },
                        "createdAt": { "value": %s },
                        "updatedAt": { "value": %s }
                      }
                    }
                  }
                }
                """.formatted(
                vertexType,
                escape(item.id()),
                quote(item.title()),
                quote(item.description()),
                item.completed(),
                quote(item.createdAt().toString()),
                quote(item.updatedAt().toString())
        );
        HttpRequest request = requestBuilder(graphPath(""))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        execute(request, "Failed to upsert TODO in TigerGraph");
    }

    private TodoItem toItem(JsonNode vertexNode) {
        String id = vertexNode.path("v_id").asText();
        JsonNode attributes = vertexNode.path("attributes");
        return new TodoItem(
                id,
                nullIfEmpty(attributes.path("title").asText(null)),
                nullIfEmpty(attributes.path("description").asText(null)),
                attributes.path("completed").asBoolean(false),
                OffsetDateTime.parse(attributes.path("createdAt").asText()),
                OffsetDateTime.parse(attributes.path("updatedAt").asText())
        );
    }

    private JsonNode execute(HttpRequest request, String failureMessage) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(failureMessage + " (HTTP " + response.statusCode() + "): " + response.body());
            }
            JsonNode root = objectMapper.readTree(response.body());
            if (root.path("error").asBoolean(false)) {
                throw new IllegalStateException(failureMessage + ": " + root.path("message").asText("unknown TigerGraph error"));
            }
            return root;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(failureMessage, ex);
        } catch (IOException ex) {
            throw new IllegalStateException(failureMessage, ex);
        }
    }

    private HttpRequest.Builder requestBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Accept", MediaType.APPLICATION_JSON_VALUE);
        if (!authToken.isEmpty()) {
            builder.header("Authorization", "Bearer " + authToken);
        }
        return builder;
    }

    private String graphPath(String suffix) {
        String normalizedBase = baseUrl.toLowerCase();
        String graphPrefix = normalizedBase.endsWith("/restpp") || normalizedBase.contains("/restpp/")
                ? "/graph/"
                : "/restpp/graph/";
        return graphPrefix + graphName + suffix;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String quote(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + escape(value) + "\"";
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private String nullIfEmpty(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
