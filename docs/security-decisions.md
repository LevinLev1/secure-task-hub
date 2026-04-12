# Security Decisions

## Scope

The project is designed to show practical and explainable security controls for a junior DevSecOps portfolio. The controls are intentionally modest and visible.

## Decisions

### Authentication with JWT

Reason:

- simple to understand
- easy to test with `curl` or Postman
- useful for showing stateless service protection in Kubernetes

Trade-off:

- no refresh token flow
- no identity provider integration
- not intended as a full enterprise IAM example

### Password hashing with BCrypt

Reason:

- avoids storing plain passwords
- standard and recognizable control for interviews

### Shared secret via environment variables

Reason:

- keeps secrets out of source code
- maps cleanly to local Docker and Kubernetes `Secret`

Trade-off:

- good for demo setup
- in a more advanced version, this could be moved to Vault or sealed secrets

### Limited actuator exposure

Reason:

- keeps platform health checks available
- reduces unnecessary operational surface

Enabled endpoints:

- `health`
- `info`

### Non-root containers

Reason:

- demonstrates a basic but important container hardening step

### NetworkPolicy

Reason:

- gives a concrete example of restricting pod communication
- shows familiarity with platform-level security, even in a small project

### Scanning strategy

The CI pipeline uses:

- `Trivy` filesystem scan for vulnerabilities, secrets, and misconfiguration
- `Trivy` image scan for container images
- `Grype` image scan as a second scanner

Reason:

- this matches the vacancy direction you described
- it gives you something concrete to discuss in interviews: different scanners, fail thresholds, image hygiene, and CI gatekeeping

## Threats this project tries to reduce

- plain-text password storage
- unauthenticated access to task APIs
- privilege escalation between regular users and admins
- accidental secret leakage into repository history
- shipping images with known severe vulnerabilities
- deploying containers without basic hardening

## Threats intentionally left out for simplicity

- SSO and external identity providers
- service-to-service mutual TLS
- advanced rate limiting
- WAF integration
- supply-chain signing and provenance
- admission controllers and policy engines

These are good future extensions, but not required for a first convincing pet project.
