# Changelog

All notable changes to this project are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versioning follows SemVer.

## [Unreleased]

### Target version
- `0.1.1`

### Added
- Security-negative integration tests for auth/task flows (invalid credentials, invalid/missing token, forbidden access, invalid payload)
- Local pre-commit hook configuration with `gitleaks` and baseline file hygiene checks
- Manual/feature DAST workflow with OWASP ZAP baseline and report artifacts

### Changed
- CI naming and documentation now explicitly separates Secret Detection, Source SCA, SAST, IaC policy checks, and Binary SCA
- Browser-facing security headers strengthened (Permissions-Policy, COOP, COEP, CORP) and CSP policy updated with `form-action`
- Documentation updated with accepted demo risk for Swagger/CSP warnings and production hardening notes

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
