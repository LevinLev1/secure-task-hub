# SecureTaskHub

Pet DevSecOps portfolio: two Spring Boot microservices, PostgreSQL, Docker/Kubernetes, CI security gates.

- Repository: [LevinLev1/secure-task-hub](https://github.com/LevinLev1/secure-task-hub/tree/main)
- Version: `0.1.2`

## Three ways to run (pick one)

These are the **runtime** options. Maven is only for **build and tests** (see [Build and test](#build-and-test-not-a-run-mode)).

| Option | What runs where | Guide |
|--------|-----------------|-------|
| **A. Docker Compose** | Postgres + auth + task in containers | [docs/run-with-docker-compose.md](docs/run-with-docker-compose.md) |
| **B. IntelliJ IDEA** | Postgres in Docker; services on host (debug) | [docs/run-with-idea.md](docs/run-with-idea.md) |
| **C. Kubernetes (`kind`)** | Full stack in local K8s cluster | [docs/run-with-kind.md](docs/run-with-kind.md) |

**Quick start (A — Docker Compose):**

```bash
docker compose -f infra/docker/docker-compose.yml up --build
```

| Service | Port | Swagger |
|---------|------|---------|
| `auth-service` | 8081 | http://localhost:8081/swagger-ui.html |
| `task-service` | 8082 | http://localhost:8082/swagger-ui.html |

Start order: PostgreSQL → auth (Flyway) → task (automatic in Compose).

**Windows:** PowerShell, Git Bash, or WSL. `make` is optional — [Run without Make](#run-without-make).

## Repository layout

```
secure-task-hub/
├── pom.xml                         # Maven parent (modules below)
├── services/
│   ├── shared-common/              # correlation ID, audit, JWT helpers, shared security headers
│   ├── auth-service/               # :8081 — register/login, JWT, Flyway owner
│   │   └── src/main/resources/db/migration/   # V1 schema, V2 audit_log
│   └── task-service/               # :8082 — task CRUD, JWT validation
├── infra/
│   ├── docker/docker-compose.yml   # local all-in-one runtime
│   └── kubernetes/                 # K8s manifests + kustomization for kind
├── docs/                           # architecture, security, run guides
├── Makefile                        # shortcuts (optional; needs make)
├── api.http                        # HTTP client examples (IDEA / VS Code)
└── .github/workflows/              # CI, DAST, release
```

**Where to look first**

| Question | Location |
|----------|----------|
| DB schema / migrations | `services/auth-service/src/main/resources/db/migration/` |
| JWT issuance | `services/auth-service/.../service/JwtService.java` |
| JWT validation | `services/task-service/.../security/JwtAuthenticationFilter.java` |
| Task business rules | `services/task-service/.../service/TaskService.java` |

## What is implemented

| Component | Responsibility |
|-----------|----------------|
| `auth-service` | Registration, login, BCrypt, JWT, roles |
| `task-service` | Protected task CRUD, owner scoping, `ROLE_ADMIN` override |
| PostgreSQL | Shared database (demo) |
| Flyway | Migrations in auth only; task uses `ddl-auto: validate` |
| Observability | JSON logs, `X-Correlation-Id`, `audit_log` table |

Architecture diagram and request flow: [docs/architecture.md](docs/architecture.md).

## Security controls

- Spring Security and stateless JWT auth in both services
- Role model: `ROLE_USER` and `ROLE_ADMIN`
- Password hashing with `BCrypt`
- Secrets provided via environment variables / Kubernetes `Secret`
- Container hardening: non-root, reduced capabilities, read-only root filesystem
- Additional browser-facing hardening headers (`Permissions-Policy`, `COOP`, `COEP`, `CORP`)
- Kubernetes health probes, resource limits, and `NetworkPolicy`
- CI security checks with Trivy, Grype, Semgrep, and Checkov

Detailed rationale: [docs/security-decisions.md](docs/security-decisions.md).

## Run locally

See [Three ways to run](#three-ways-to-run-pick-one) above. Short recap:

### Option A: Docker Compose (recommended)

```bash
docker compose -f infra/docker/docker-compose.yml up --build
```

Details: [docs/run-with-docker-compose.md](docs/run-with-docker-compose.md)

### Option B: IntelliJ IDEA (DB in Docker, services on host)

1. `docker compose -f infra/docker/docker-compose.yml up -d postgres`
2. Debug auth → then task. Details: [docs/run-with-idea.md](docs/run-with-idea.md)

### Option C: Kubernetes (`kind`)

```bash
make kind-up
# then: make pf-auth  and  make pf-task  (separate terminals)
```

Details: [docs/run-with-kind.md](docs/run-with-kind.md) (includes steps **without** `make`).

### Run without Make

| Goal | Command (repo root) |
|------|---------------------|
| Full stack | `docker compose -f infra/docker/docker-compose.yml up --build` |
| Postgres only | `docker compose -f infra/docker/docker-compose.yml up -d postgres` |
| Stop stack | `docker compose -f infra/docker/docker-compose.yml down` |
| Reset DB volume | `docker compose -f infra/docker/docker-compose.yml down -v` |
| kind (no make) | See [docs/run-with-kind.md](docs/run-with-kind.md) |

## Build and test (not a run mode)

Maven `verify` compiles the project and runs unit/integration tests (Testcontainers needs Docker). Use in IDEA or CI — **does not start** the app for manual API testing.

```text
IDEA: Maven tool window → secure-task-hub → Lifecycle → verify
```

For trying the API use **Docker Compose**, **IDEA run**, or **kind** above.

## Quick API check

```bash
# 1. Register
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"alice\",\"email\":\"alice@example.com\",\"password\":\"StrongPass123\"}"

# 2. Login — copy accessToken from response
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"alice\",\"password\":\"StrongPass123\"}"

# 3. Create task
curl -X POST http://localhost:8082/api/tasks \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Review CI\",\"description\":\"Check gates\",\"status\":\"OPEN\"}"
```

Or use `api.http` in the IDE (replace `<TOKEN>` after login).

## Documentation

| Topic | File |
|-------|------|
| **Run: Docker Compose** | [docs/run-with-docker-compose.md](docs/run-with-docker-compose.md) |
| **Run: IDEA debug** | [docs/run-with-idea.md](docs/run-with-idea.md) |
| **Run: kind / K8s** | [docs/run-with-kind.md](docs/run-with-kind.md) |
| Architecture | [docs/architecture.md](docs/architecture.md) |
| Security decisions | [docs/security-decisions.md](docs/security-decisions.md) |
| Versioning / branches | [docs/versioning.md](docs/versioning.md) |
| Changelog | [CHANGELOG.md](CHANGELOG.md) |
| CI pipeline details | [.github/workflows/ci.yml](.github/workflows/ci.yml) |

## CI quality gates

Workflow: [.github/workflows/ci.yml](.github/workflows/ci.yml)

| Stage | Tool | Why it is used | Fails when |
| --- | --- | --- | --- |
| Stage 1 | Maven verify + Testcontainers | Prove functional correctness before security gates | Tests fail |
| Stage 1 | Trivy fs (`secret`) — **Secret Detection** | Secret detection in source/config files before build | `HIGH`/`CRITICAL` findings |
| Stage 1 | Trivy fs (`vuln`) — **Source SCA** | Dependency vulnerability scan at source/filesystem level | `HIGH`/`CRITICAL` findings |
| Stage 1 | Trivy fs (`misconfig`) — IaC checks | IaC/config misconfiguration scan on repository files | `HIGH`/`CRITICAL` findings |
| Stage 1b | **SAST (Semgrep)** (`p/java`, `p/security-audit`) | Static analysis for Java/security anti-patterns | Rule violations |
| Stage 1c | **IaC policy (Checkov/K8s)** | Kubernetes policy-as-code checks | Non-skipped failing checks |
| Stage 2 | Trivy image + Grype — **Binary SCA** | Vulnerability scan of built Docker image artifacts | High/Critical vulnerability threshold |
| Stage 2 | Trivy config (`infra/k8s`) — IaC checks | Misconfig scan on Kubernetes manifests as deployed | `HIGH`/`CRITICAL` findings |
| Stage 3 (manual/feature) | OWASP ZAP baseline ([`.github/workflows/dast.yml`](.github/workflows/dast.yml)) | DAST smoke security scan against running services | Fails on scan/runtime errors, uploads report artifacts |

## Versioning

SemVer `0.1.2`; release tags `vX.Y.Z`. Branch `main` = stable; `feature/*` = development.

Details: [docs/versioning.md](docs/versioning.md).

## Pre-commit (optional)

```bash
pip install pre-commit
pre-commit install
pre-commit run --all-files
```

Includes `gitleaks` and basic file hygiene hooks.
