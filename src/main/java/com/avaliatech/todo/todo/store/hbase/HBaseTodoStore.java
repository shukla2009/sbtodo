package com.avaliatech.todo.todo.store.hbase;

import com.avaliatech.todo.todo.CreateTodoRequest;
import com.avaliatech.todo.todo.TodoItem;
import com.avaliatech.todo.todo.TodoNotFoundException;
import com.avaliatech.todo.todo.UpdateTodoRequest;
import com.avaliatech.todo.todo.store.TodoStore;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.annotation.PreDestroy;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("hbase")
public class HBaseTodoStore implements TodoStore {
    private static final byte[] TITLE = Bytes.toBytes("title");
    private static final byte[] DESCRIPTION = Bytes.toBytes("description");
    private static final byte[] COMPLETED = Bytes.toBytes("completed");
    private static final byte[] CREATED_AT = Bytes.toBytes("createdAt");
    private static final byte[] UPDATED_AT = Bytes.toBytes("updatedAt");

    private final Connection connection;
    private final TableName tableName;
    private final byte[] columnFamily;

    public HBaseTodoStore(
            @Value("${hbase.zookeeper.quorum}") String quorum,
            @Value("${hbase.zookeeper.property.clientPort}") String clientPort,
            @Value("${hbase.table.name}") String tableName,
            @Value("${hbase.column.family}") String columnFamily
    ) {
        try {
            Configuration config = HBaseConfiguration.create();
            config.set("hbase.zookeeper.quorum", quorum);
            config.set("hbase.zookeeper.property.clientPort", clientPort);
            this.connection = ConnectionFactory.createConnection(config);
            this.tableName = TableName.valueOf(tableName);
            this.columnFamily = Bytes.toBytes(columnFamily);
            // ensureTable();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to initialize HBase connection", ex);
        }
    }

    @Override
    public List<TodoItem> findAll() {
        List<TodoItem> items = new ArrayList<>();
        try (Table table = connection.getTable(tableName);
             var scanner = table.getScanner(new Scan())) {
            for (Result result : scanner) {
                items.add(toItem(result));
            }
            return items;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read TODOs from HBase", ex);
        }
    }

    @Override
    public Optional<TodoItem> findById(String id) {
        try (Table table = connection.getTable(tableName)) {
            Result result = table.get(new Get(Bytes.toBytes(id)));
            if (result.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(toItem(result));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to fetch TODO from HBase", ex);
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
        save(item);
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
        save(updated);
        return updated;
    }

    @Override
    public void delete(String id) {
        if (findById(id).isEmpty()) {
            throw new TodoNotFoundException(id);
        }
        try (Table table = connection.getTable(tableName)) {
            table.delete(new Delete(Bytes.toBytes(id)));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete TODO from HBase", ex);
        }
    }

    private void save(TodoItem item) {
        try (Table table = connection.getTable(tableName)) {
            Put put = new Put(Bytes.toBytes(item.id()));
            put.addColumn(columnFamily, TITLE, bytes(item.title()));
            put.addColumn(columnFamily, DESCRIPTION, bytes(item.description()));
            put.addColumn(columnFamily, COMPLETED, bytes(String.valueOf(item.completed())));
            put.addColumn(columnFamily, CREATED_AT, bytes(item.createdAt().toString()));
            put.addColumn(columnFamily, UPDATED_AT, bytes(item.updatedAt().toString()));
            table.put(put);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to save TODO to HBase", ex);
        }
    }

    private TodoItem toItem(Result result) {
        String id = Bytes.toString(result.getRow());
        String title = stringValue(result, TITLE);
        String description = stringValue(result, DESCRIPTION);
        boolean completed = Boolean.parseBoolean(stringValue(result, COMPLETED));
        OffsetDateTime createdAt = OffsetDateTime.parse(stringValue(result, CREATED_AT));
        OffsetDateTime updatedAt = OffsetDateTime.parse(stringValue(result, UPDATED_AT));
        return new TodoItem(id, title, description, completed, createdAt, updatedAt);
    }

    private String stringValue(Result result, byte[] qualifier) {
        byte[] value = result.getValue(columnFamily, qualifier);
        return value == null ? null : Bytes.toString(value);
    }

    private byte[] bytes(String value) {
        return value == null ? Bytes.toBytes("") : value.getBytes(StandardCharsets.UTF_8);
    }

    private void ensureTable() throws IOException {
        try (Admin admin = connection.getAdmin()) {
            if (!admin.tableExists(tableName)) {
                admin.createTable(
                        TableDescriptorBuilder.newBuilder(tableName)
                                .setColumnFamily(ColumnFamilyDescriptorBuilder.of(columnFamily))
                                .build()
                );
            }
        }
    }

    @PreDestroy
    void closeConnection() throws IOException {
        connection.close();
    }
}
