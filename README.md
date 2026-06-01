# sbtodo

Simple TODO API built with Spring Boot to compare multiple storage backends.

> This is a learning project and is **not production-ready**.
> It is intentionally focused on experimentation over hardening.

## Tech Stack

- Java 21
- Spring Boot
- Maven
- Storage profiles: `h2` (default), `postgres`, `oracle`, `hbase`, `couchbase`, `tigergraph`

## Prerequisites

- Java 21
- Docker + Docker Compose

## Summary

This project helps you run the same TODO API against different storage backends and verify behavior consistently.

Expected outcome:

- Selected backend service is running in Docker.
- Backend initialization (if required) has completed successfully.
- Backend portal (if available) is reachable so you can confirm readiness/data.
- Spring Boot app is running with the matching profile.
- TODO APIs work in Swagger for create and read flows.

## General Execution Steps

1. Choose one backend profile (`h2`, `postgres`, `oracle`, `hbase`, `couchbase`, or `tigergraph`).
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

- H2 console (if used): username `sa`, password empty

### 2) Postgres

Start Docker service:

```bash
docker compose up -d postgres
```

Initialization: none required.

Portal: none configured in this project for Postgres.

- Postgres: username `todo`, password `todo`, database `todos`

Run app with profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

### 3) Oracle

Start Docker service:

```bash
docker compose up -d oracle
```

Initialization: none required.

- Oracle container user: `sys` (password from `ORACLE_PASSWORD`) or app user `todo` / `todo`
- Service name for app connection: `FREEPDB1`

Run app with profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=oracle
```

### 4) HBase

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
- HBase portal: no login configured in this setup

Run app with profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=hbase
```

### 5) Couchbase

Start Docker service:

```bash
docker compose up -d couchbase
```

Initialize:

```bash
docker compose exec couchbase sh /scripts/couchbase-init.sh
```

Portal: `http://localhost:8091`

- Couchbase portal: username `Administrator`, password `password`

Run app with profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=couchbase
```

### 6) TigerGraph

Start Docker service:

```bash
docker compose up -d tigergraph
```

Initialize:

```bash
docker compose exec tigergraph bash
gsql gsql/tg-todo.schema.gsql
exit
```

Portal/API endpoint: `http://localhost:14240`

- TigerGraph: username `tigergraph`, password `tigergraph`

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
