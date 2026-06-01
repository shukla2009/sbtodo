# sbtodo

Simple TODO API built with Spring Boot to compare multiple storage backends.

> This is a learning project and is **not production-ready**.
> It is intentionally focused on experimentation over hardening.

## Tech Stack

- Java 21
- Spring Boot
- Maven
- Storage profiles: `h2` (default), `postgres`, `hbase`, `couchbase`, `tigergraph`

## Prerequisites

- Java 21
- Docker + Docker Compose

## Run Infrastructure (Docker)

Start all backing services:

```bash
docker compose up -d
```

Stop all backing services:

```bash
docker compose down
```

Reset all backing service data volumes:

```bash
docker compose down -v
```

### One-time init for some backends

Couchbase and TigerGraph need extra initialization when first started.

Initialize Couchbase bucket/scope/collection:

```bash
docker compose exec couchbase sh /scripts/couchbase-init.sh
```

Initialize TigerGraph schema:

```bash
docker compose exec tigergraph gsql -f /home/tigergraph/gsql/tg-todo.schema.gsql
```

## Run the App

From the project root:

```bash
./mvnw spring-boot:run
```

By default, the app uses the `h2` profile.

API base URL: `http://localhost:8080/api/v1/todos`

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Run by Profile

Use this format:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=<profile>
```

### 1) H2 (default)

No extra setup required.

```bash
./mvnw spring-boot:run
```

Optional H2 console: `http://localhost:8080/h2-console`

### 2) Postgres

Start Postgres service:

```bash
docker compose up -d postgres
```

Run app:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

### 3) HBase

Start HBase service:

```bash
docker compose up -d hbase
```

Run app:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=hbase
```

### 4) Couchbase

Start Couchbase service:

```bash
docker compose up -d couchbase
```

Initialize Couchbase:

```bash
docker compose exec couchbase sh /scripts/couchbase-init.sh
```

Run app:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=couchbase
```

### 5) TigerGraph

Start TigerGraph service:

```bash
docker compose up -d tigergraph
```

Initialize TigerGraph graph schema:

```bash
docker compose exec tigergraph gsql -f gsql/tg-todo.schema.gsql
```

Run app:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=tigergraph
```

## Build and Test

Run tests:

```bash
./mvnw test
```

Build jar:

```bash
./mvnw clean package
```

Run jar (default `h2`):

```bash
java -jar target/todo-0.0.1-SNAPSHOT.jar
```

Run jar with profile:

```bash
java -jar target/todo-0.0.1-SNAPSHOT.jar --spring.profiles.active=postgres
```
