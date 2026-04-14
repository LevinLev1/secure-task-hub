# Security Decisions

## Purpose

This document explains why each security control exists in this project and what trade-offs were accepted for a portfolio-sized implementation.

## Application security controls

### JWT-based authentication

- **Why**: easy to explain, test, and integrate with stateless microservices
- **Implemented in**: both services via Spring Security filters
- **Trade-off**: no refresh token flow and no external identity provider yet

### Password hashing with BCrypt

- **Why**: baseline requirement for secure credential storage
- **Implemented in**: `auth-service`
- **Trade-off**: no adaptive account lockout policy yet

### Role-based access control

- **Why**: demonstrate authorization beyond simple authentication
- **Implemented in**: `ROLE_USER` and `ROLE_ADMIN` model
- **Trade-off**: no fine-grained policy engine

### Reduced actuator exposure

- **Why**: keep health/readiness endpoints while limiting operational surface
- **Implemented in**: Spring Actuator endpoint allowlist
- **Trade-off**: no auth-protected operational dashboard

## Container and Kubernetes controls

### Non-root containers and runtime hardening

- **Why**: reduce blast radius of runtime exploits
- **Implemented in**: `runAsNonRoot`, dropped capabilities, read-only root filesystem
- **Trade-off**: additional config (`/tmp`, JVM options) needed for Java runtime behavior

### Config and secrets separation

- **Why**: avoid hardcoded secrets in source code and manifests
- **Implemented in**: Kubernetes `ConfigMap` + `Secret`, env-based injection
- **Trade-off**: demo uses env vars for simplicity; production can move to secret volumes or external secret stores

### NetworkPolicy baseline

- **Why**: show platform-level traffic control and namespace isolation basics
- **Implemented in**: explicit ingress/egress policy in `infra/k8s/base/secure-task-hub.yaml`
- **Trade-off**: intentionally minimal policy scope for readability

## CI security gates

### Trivy filesystem scan

- **Why**: detect vulnerable dependencies, leaked secrets, and IaC misconfigurations before image build
- **Gate**: fail on `HIGH` / `CRITICAL`

### Semgrep SAST

- **Why**: identify Java/security anti-patterns in source code
- **Gate**: fail on rule violations from `p/java` and `p/security-audit`

### Checkov Kubernetes scan

- **Why**: enforce policy checks for manifests in `infra/k8s`
- **Gate**: fail on non-skipped policy violations
- **Baseline skips used intentionally in this repository**:
  - `CKV_K8S_14` and `CKV_K8S_43`: base manifest uses placeholder tags for local/dev flows; digest pinning is planned for registry-backed release manifests
  - `CKV_K8S_15`: `IfNotPresent` is acceptable for local iteration speed
  - `CKV_K8S_35`: secrets via env keep the demo setup simple; production variant should prefer mounted secret files
  - `CKV_K8S_38`: default service account token behavior is kept for demo simplicity; can be hardened with `automountServiceAccountToken: false`
  - `CKV_K8S_40`: postgres image UID constraints in official image conflict with strict "high UID" policy in this baseline

### Trivy + Grype image scans

- **Why**: two vulnerability scanners improve confidence and interview talking points
- **Gate**: fail on severe image findings
