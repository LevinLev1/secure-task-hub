# Run with IntelliJ IDEA (hybrid mode)

PostgreSQL in Docker; `auth-service` and `task-service` run locally in the IDE for breakpoints.

**Prerequisites:** Docker Desktop, JDK 17, Maven (IDEA bundled Maven is fine).

## Start order

1. PostgreSQL (Docker)
2. `auth-service` — applies Flyway migrations
3. `task-service` — expects existing schema (`ddl-auto: validate`)

## Optional `.run` templates

Shared templates in `.run/` (if visible in IDEA):

- `auth-service (Debug Local DB)`
- `task-service (Debug Local DB)`
- `SecureTaskHub Debug (Compound)` — starts both (still start auth before task on first run)

## 1. Start PostgreSQL only

From repository root:

```bash
docker compose -f infra/docker/docker-compose.yml up -d postgres
```

With `make`: `make compose-db-up`

Tail logs: `docker compose -f infra/docker/docker-compose.yml logs -f postgres`

## 2. Configure `auth-service`

| Field | Value |
|-------|-------|
| Main class | `com.securetaskhub.auth.AuthServiceApplication` |
| Module | `auth-service` |

Environment variables (Run → Modify options → Environment variables):

```env
SERVER_PORT=8081
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/securetaskhub
SPRING_DATASOURCE_USERNAME=securetaskhub
SPRING_DATASOURCE_PASSWORD=securetaskhub-dev-password
JWT_SECRET=replace-this-with-a-long-development-secret-key-123456
JWT_EXPIRATION_SECONDS=3600
```

Start in **Debug** mode first.

## 3. Configure `task-service`

| Field | Value |
|-------|-------|
| Main class | `com.securetaskhub.task.TaskServiceApplication` |
| Module | `task-service` |

Environment variables:

```env
SERVER_PORT=8082
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/securetaskhub
SPRING_DATASOURCE_USERNAME=securetaskhub
SPRING_DATASOURCE_PASSWORD=securetaskhub-dev-password
JWT_SECRET=replace-this-with-a-long-development-secret-key-123456
```

`JWT_SECRET` must match auth-service. Start in **Debug** second.

## 4. Verify

- Auth Swagger: http://localhost:8081/swagger-ui.html
- Task Swagger: http://localhost:8082/swagger-ui.html

## 5. Stop

1. Stop IDEA run configurations.
2. Stop Postgres:

```bash
docker compose -f infra/docker/docker-compose.yml stop postgres
```

With `make`: `make compose-db-down`

## Common issues

| Symptom | Fix |
|---------|-----|
| `password authentication failed for user "securetaskhub"` | Use `securetaskhub-dev-password` (same as `infra/docker/docker-compose.yml`). |
| `task-service` schema validation fails | Start `auth-service` first so Flyway creates tables. |
| Ports 8081/8082 busy | Change `SERVER_PORT` or stop the conflicting process. |

This mode is for **debugging**. For a full container demo use [run-with-docker-compose.md](run-with-docker-compose.md); for K8s use [run-with-kind.md](run-with-kind.md).
