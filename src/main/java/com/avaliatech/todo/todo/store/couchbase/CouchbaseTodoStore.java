package com.avaliatech.todo.todo.store.couchbase;

import com.avaliatech.todo.todo.CreateTodoRequest;
import com.avaliatech.todo.todo.TodoItem;
import com.avaliatech.todo.todo.TodoNotFoundException;
import com.avaliatech.todo.todo.UpdateTodoRequest;
import com.avaliatech.todo.todo.store.TodoStore;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.query.QueryScanConsistency;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("couchbase")
public class CouchbaseTodoStore implements TodoStore {

    private final Cluster cluster;
    private final String bucketName;
    private final String scopeName;
    private final String collectionName;
    private final Collection collection;

    public CouchbaseTodoStore(
            @Value("${couchbase.connection-string}") String connectionString,
            @Value("${couchbase.username}") String username,
            @Value("${couchbase.password}") String password,
            @Value("${couchbase.bucket}") String bucketName,
            @Value("${couchbase.scope}") String scopeName,
            @Value("${couchbase.collection}") String collectionName
    ) {
        this.cluster = Cluster.connect(connectionString, username, password);
        Bucket bucket = cluster.bucket(bucketName);
        bucket.waitUntilReady(Duration.ofSeconds(10));
        Scope scope = bucket.scope(scopeName);

        this.bucketName = bucketName;
        this.scopeName = scopeName;
        this.collectionName = collectionName;
        this.collection = scope.collection(collectionName);

        ensurePrimaryIndex();
    }

    @Override
    public List<TodoItem> findAll() {
        QueryResult result = cluster.query(
                "SELECT META(t).id AS id, t.* FROM `" + bucketName + "`.`" + scopeName + "`.`" + collectionName + "` t",
                queryOptions()
        );
        return result.rowsAsObject().stream().map(this::toItem).toList();
    }

    @Override
    public Optional<TodoItem> findById(String id) {
        try {
            return Optional.of(toItem(id, collection.get(id).contentAsObject()));
        } catch (com.couchbase.client.core.error.DocumentNotFoundException ex) {
            return Optional.empty();
        }
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
        collection.insert(id, toJson(item));
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
        collection.replace(id, toJson(updated));
        return updated;
    }

    @Override
    public void delete(String id) {
        try {
            collection.remove(id);
        } catch (com.couchbase.client.core.error.DocumentNotFoundException ex) {
            throw new TodoNotFoundException(id);
        }
    }

    private com.couchbase.client.java.query.QueryOptions queryOptions() {
        return com.couchbase.client.java.query.QueryOptions.queryOptions()
                .scanConsistency(QueryScanConsistency.REQUEST_PLUS);
    }

    private void ensurePrimaryIndex() {
        try {
            cluster.query(
                    "CREATE PRIMARY INDEX IF NOT EXISTS ON `"
                            + bucketName + "`.`" + scopeName + "`.`" + collectionName + "`",
                    queryOptions()
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create Couchbase primary index", ex);
        }
    }

    private TodoItem toItem(JsonObject row) {
        return toItem(row.getString("id"), row);
    }

    private TodoItem toItem(String id, JsonObject document) {
        return new TodoItem(
                id,
                document.getString("title"),
                document.getString("description"),
                document.getBoolean("completed"),
                OffsetDateTime.parse(document.getString("createdAt")),
                OffsetDateTime.parse(document.getString("updatedAt"))
        );
    }

    private JsonObject toJson(TodoItem item) {
        return JsonObject.create()
                .put("title", item.title())
                .put("description", item.description())
                .put("completed", item.completed())
                .put("createdAt", item.createdAt().toString())
                .put("updatedAt", item.updatedAt().toString());
    }

    @PreDestroy
    void closeConnection() {
        cluster.disconnect();
    }
}
