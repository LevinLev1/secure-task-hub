# Run with Kubernetes (`kind`)

Local Kubernetes demo: same images as Docker mode, deployed to a **kind** cluster.

**Prerequisites:** Docker Desktop, [kind](https://kind.sigs.k8s.io/), [kubectl](https://kubernetes.io/docs/tasks/tools/). On Windows: Git Bash or WSL is easiest for `make`.

## Quick path (with Make)

From repository root:

```bash
make kind-up
kubectl get pods -n secure-task-hub -w
```

In **separate terminals** (after pods are Running):

```bash
make pf-auth   # localhost:8081
make pf-task   # localhost:8082
```

Swagger: http://localhost:8081/swagger-ui.html and http://localhost:8082/swagger-ui.html

Teardown:

```bash
make kind-teardown
```

## Without Make (explicit commands)

```bash
# 1. Create cluster (once)
kind create cluster --name secure-task-hub

# 2. Build images (same Dockerfiles as Compose)
docker build -f services/auth-service/Dockerfile -t secure-task-hub-auth:local .
docker build -f services/task-service/Dockerfile -t secure-task-hub-task:local .

# 3. Load images into kind
kind load docker-image secure-task-hub-auth:local --name secure-task-hub
kind load docker-image secure-task-hub-task:local --name secure-task-hub

# 4. Apply manifests (kustomize rewrites GHCR placeholders to :local tags)
kubectl apply -k infra/kubernetes

# 5. Wait for pods
kubectl get pods -n secure-task-hub -w

# 6. Port-forward (separate terminals)
kubectl -n secure-task-hub port-forward svc/auth-service 8081:8081
kubectl -n secure-task-hub port-forward svc/task-service 8082:8082
```

## What gets deployed

- Namespace: `secure-task-hub`
- Manifests: `infra/kubernetes/base/secure-task-hub.yaml`
- Local image overrides: `infra/kubernetes/kustomization.yaml` (`:local` tags)

## Secrets in kind (demo)

K8s `Secret` `app-secrets` in the manifest uses placeholder values. For a local kind demo, ensure JWT and DB password are consistent if you change them — see `infra/kubernetes/base/secure-task-hub.yaml`.

## Common issues

| Symptom | Fix |
|---------|-----|
| `ImagePullBackOff` | Run `kind load docker-image ...` after every image rebuild. |
| Auth/task pod restarts / `failed liveness probe` | Java on kind starts slowly; manifests use `startupProbe` (up to ~5 min). Re-apply after pull. |
| Cannot reach Swagger on localhost | Port-forward must be running (`make pf-auth` / `pf-task`). |
| Port 8081/8082 already in use | Stop Docker Compose stack first: `docker compose -f infra/docker/docker-compose.yml down`. |

## Relation to other run modes

| Mode | Use when |
|------|----------|
| [Docker Compose](run-with-docker-compose.md) | Fastest local demo, no kubectl |
| [IDEA hybrid](run-with-idea.md) | Breakpoints, step debugging |
| **kind (this doc)** | Portfolio K8s / probes / NetworkPolicy demo |

Maven `verify` is **not** a run mode — it only builds and runs tests (CI / pre-push).
