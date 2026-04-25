## DB in Docker, services in local

## Optional `.run` templates

If you use IntelliJ IDEA, this repository includes optional shared run templates in `.run/`:

- `auth-service (Debug Local DB)`
- `task-service (Debug Local DB)`
- `SecureTaskHub Debug (Compound)`

You can use them as-is or create your own personal run configurations by hand.

## 1) Start only PostgreSQL

From repo root:

With `make`:

```bash
make compose-db-up
```

Without `make`:

```bash
docker compose -f infra/docker/docker-compose.yml up -d postgres
```

DB logs:

With `make`:

```bash
make compose-db-logs
```

Without `make`:

```bash
docker compose -f infra/docker/docker-compose.yml logs -f postgres
```

## 2) Configure IDEA run/debug for `auth-service`

Use it if tou dont want ro use .run 

- Main class: `com.example.authservice.AuthServiceApplication`
- Module/classpath: `auth-service`
- Environment variables:

On the right from "build and run" press Modify options and click on Environment variables
Add near variables 
```env
SERVER_PORT=8081
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/securetaskhub
SPRING_DATASOURCE_USERNAME=securetaskhub
SPRING_DATASOURCE_PASSWORD=securetaskhub-dev-password
JWT_SECRET=replace-this-with-a-long-development-secret-key-123456
JWT_EXPIRATION_SECONDS=3600
```

Start `auth-service` in **Debug** first.

## 3) Configure IDEA run/debug for `task-service`

- Main class: `com.example.taskservice.TaskServiceApplication`
- Module/classpath: `task-service`
- Environment variables:

```env
SERVER_PORT=8082
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/securetaskhub
SPRING_DATASOURCE_USERNAME=securetaskhub
SPRING_DATASOURCE_PASSWORD=securetaskhub-dev-password
JWT_SECRET=replace-this-with-a-long-development-secret-key-123456
```

Start `task-service` in **Debug** second.

## 4) Verification

- Auth Swagger: `http://localhost:8081/swagger-ui.html`
- Task Swagger: `http://localhost:8082/swagger-ui.html`

## 5) Stop

- Stop IDEA services via IDE.
- Stop DB container:

With `make`:

```bash
make compose-db-down
```

Without `make`:

```bash
docker compose -f infra/docker/docker-compose.yml stop postgres
```

## Common issues

- `password authentication failed for user "securetaskhub"`
  - Check DB username/password env vars and PostgreSQL user settings.
- `task-service` fails on schema validation
  - `auth-service` was not started first (migrations were not applied).
- Ports `8081`/`8082` are busy
  - Change `SERVER_PORT` or stop conflicting process.
