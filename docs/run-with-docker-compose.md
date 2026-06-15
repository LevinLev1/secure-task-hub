# Run with Docker Compose

All components run in containers: PostgreSQL, `auth-service`, `task-service`.

**Prerequisites:** Docker Desktop (Windows / macOS / Linux).

## Start full stack

From repository root:

```bash
docker compose -f infra/docker/docker-compose.yml up --build
```

With `make`: `make compose-up`

| Service | Port | Swagger |
|---------|------|---------|
| auth-service | 8081 | http://localhost:8081/swagger-ui.html |
| task-service | 8082 | http://localhost:8082/swagger-ui.html |

Compose starts services in order: **postgres** (healthy) → **auth-service** (Flyway migrations) → **task-service**.

Images are built from `services/*/Dockerfile` (multi-stage Maven build inside Docker — you do **not** need local `mvn` to run this mode).

## Stop

```bash
docker compose -f infra/docker/docker-compose.yml down
```

With `make`: `make compose-down`

## Reset database (clean Flyway slate)

```bash
docker compose -f infra/docker/docker-compose.yml down -v
```

With `make`: `make compose-down-v`

## Logs

```bash
docker compose -f infra/docker/docker-compose.yml logs -f auth-service
docker compose -f infra/docker/docker-compose.yml logs -f task-service
```

## Configuration

Runtime env vars are in `infra/docker/docker-compose.yml` (ports, JDBC URL, `JWT_SECRET`).

Default demo `JWT_SECRET` matches local IDEA workflow — keep the same value across modes if you mix them.

## Common issues

| Symptom | Fix |
|---------|-----|
| `task-service` exits on startup | Wait for `auth-service` healthy; task needs migrated schema. |
| Port 5432/8081/8082 already in use | Stop other stacks or change host port mapping in compose file. |
| Build fails after code changes | Rebuild: `docker compose ... up --build` (picks up `shared-common` via Dockerfile). |
