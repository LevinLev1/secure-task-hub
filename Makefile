# SecureTaskHub — local DX (Docker Compose + optional kind).
# On Windows: use Git Bash, WSL, or "make" from a dev environment where kind/kubectl/docker are on PATH.
# Requires: Docker, kind, kubectl.

CLUSTER_NAME ?= secure-task-hub
COMPOSE := docker compose -f infra/docker/docker-compose.yml

.PHONY: help
help:
	@echo "Compose:"
	@echo "  make compose-up      - build and start postgres + auth + task (from repo root)"
	@echo "  make compose-db-up   - start only postgres for IDE debug workflow"
	@echo "  make compose-db-logs - tail postgres logs for local debug"
	@echo "  make compose-db-down - stop only postgres service"
	@echo "  make compose-down    - stop compose stack"
	@echo "  make compose-down-v  - stop and remove postgres volume (Flyway clean slate)"
	@echo ""
	@echo "kind (local Kubernetes):"
	@echo "  make kind-cluster    - create cluster if missing ($(CLUSTER_NAME))"
	@echo "  make docker-build-k8s - build auth/task images tagged :local for kind"
	@echo "  make kind-load        - load :local images into kind"
	@echo "  make k8s-apply-kind  - kubectl apply -k infra/kubernetes (local images)"
	@echo "  make kind-up         - cluster + build + load + apply (full local K8s demo)"
	@echo "  make kind-teardown   - delete kind cluster"
	@echo "  make pf-auth         - port-forward auth8081 (run in a separate terminal)"
	@echo "  make pf-task         - port-forward task 8082"

.PHONY: compose-up compose-db-up compose-db-logs compose-db-down compose-down compose-down-v
compose-up:
	$(COMPOSE) up --build

compose-db-up:
	$(COMPOSE) up -d postgres

compose-db-logs:
	$(COMPOSE) logs -f postgres

compose-db-down:
	$(COMPOSE) stop postgres

compose-down:
	$(COMPOSE) down

compose-down-v:
	$(COMPOSE) down -v

.PHONY: docker-build-k8s
docker-build-k8s:
	docker build -f services/auth-service/Dockerfile -t secure-task-hub-auth:local .
	docker build -f services/task-service/Dockerfile -t secure-task-hub-task:local .

.PHONY: kind-cluster
kind-cluster:
	@kind get clusters 2>/dev/null | grep -q '$(CLUSTER_NAME)' || kind create cluster --name '$(CLUSTER_NAME)'

.PHONY: kind-load
kind-load: docker-build-k8s
	kind load docker-image secure-task-hub-auth:local --name '$(CLUSTER_NAME)'
	kind load docker-image secure-task-hub-task:local --name '$(CLUSTER_NAME)'

.PHONY: k8s-apply-kind
k8s-apply-kind:
	kubectl apply -k infra/kubernetes

.PHONY: kind-up
kind-up: kind-cluster kind-load k8s-apply-kind
	@echo ""
	@echo "Cluster ready. Wait for pods: kubectl get pods -n secure-task-hub -w"
	@echo "Then (separate terminals): make pf-auth   and   make pf-task"
	@echo "Swagger: http://localhost:8081/swagger-ui.html  http://localhost:8082/swagger-ui.html"

.PHONY: kind-teardown
kind-teardown:
	kind delete cluster --name '$(CLUSTER_NAME)'

.PHONY: pf-auth pf-task
pf-auth:
	kubectl -n secure-task-hub port-forward svc/auth-service 8081:8081

pf-task:
	kubectl -n secure-task-hub port-forward svc/task-service 8082:8082
