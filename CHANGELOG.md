# Changelog

All notable changes to this project are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versioning follows SemVer.

## [Unreleased]

### Planned
- Demo branch with intentionally insecure configurations for scanner walkthroughs
- OAuth2/OIDC feature branch

## [0.1.0] - 2026-04-14

### Added
- `auth-service` and `task-service` microservice baseline with JWT auth
- Docker Compose runtime with PostgreSQL
- Kubernetes manifests for namespace, deployments, services, probes, and network policy
- Local kind flow via `infra/k8s/kustomization.yaml` and `Makefile` targets
- CI pipeline with Maven verify, Trivy, Grype, Semgrep, and Checkov
- JSON logging, correlation ID filter, and audit trail persistence
- Release workflow for SemVer tags (`vX.Y.Z`)

### Changed
- Project and module versions aligned to `0.1.0`
- API docs versioning aligned to project version

### Notes
- This is the first portfolio-ready baseline release line for SecureTaskHub.
