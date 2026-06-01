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

## Default Credentials

Use these defaults when logging into database portals/tools in this project:

- Postgres: username `todo`, password `todo`, database `todos`
- Couchbase portal: username `Administrator`, password `password`
- H2 console (if used): username `sa`, password empty
- HBase portal: no login configured in this setup
- TigerGraph: no auth token configured by default (`tigergraph.auth-token` is empty)

## Summary

This project helps you run the same TODO API against different storage backends and verify behavior consistently.

Expected outcome:

- Selected backend service is running in Docker.
- Backend initialization (if required) has completed successfully.
- Backend portal (if available) is reachable so you can confirm readiness/data.
- Spring Boot app is running with the matching profile.
- TODO APIs work in Swagger for create and read flows.

## General Execution Steps

1. Choose one backend profile (`h2`, `postgres`, `hbase`, `couchbase`, or `tigergraph`).
2. Start that backend service in Docker (`docker compose up -d <service>`) if needed.
3. Run backend initialization command if that profile requires it.
4. Open the backend portal (if available) and verify service is ready.
5. Start the app with the same Spring profile.
6. Open Swagger and test create/read TODO endpoints.
7. Recheck backend portal to confirm data is available in the selected database.

## Run by Profile

### 1) H2 (default)

No Docker setup or initialization required.

```bash
./mvnw spring-boot:run
```

Portal (optional): `http://localhost:8080/h2-console`

### 2) Postgres

Start Docker service:

```bash
docker compose up -d postgres
```

Initialization: none required.

Portal: none configured in this project for Postgres.

Run app with profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

### 3) HBase

Start Docker service:

```bash
docker compose up -d hbase
```

Initialize:

```bash
docker compose exec hbase sh /scripts/hbase-init.sh
```

Portal:

- HBase Master UI: `http://localhost:16010`
- HBase RegionServer UI: `http://localhost:16030`

Run app with profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=hbase
```

### 4) Couchbase

Start Docker service:

```bash
docker compose up -d couchbase
```

Initialize:

```bash
docker compose exec couchbase sh /scripts/couchbase-init.sh
```

Portal: `http://localhost:8091`

Run app with profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=couchbase
```

### 5) TigerGraph

Start Docker service:

```bash
docker compose up -d tigergraph
```

Initialize:

```bash
docker compose exec tigergraph gsql -f /home/tigergraph/gsql/tg-todo.schema.gsql
```

Portal/API endpoint: `http://localhost:14240`

Run app with profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=tigergraph
```

## Test with Swagger

API base URL: `http://localhost:8080/api/v1/todos`

Swagger UI: `http://localhost:8080/swagger-ui.html`

1. Start one backend profile from sections above.
2. Open Swagger UI.
3. Create a TODO using `POST /api/v1/todos`.
4. Verify data using `GET /api/v1/todos` and `GET /api/v1/todos/{id}`.
5. If a portal is available for that backend, open it and verify the TODO exists.

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
