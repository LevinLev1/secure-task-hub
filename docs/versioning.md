# Versioning

## Maven (SemVer)

- The reactor version lives in the root `pom.xml` (`<version>`), currently aligned with **SemVer** `MAJOR.MINOR.PATCH` (e.g. `0.1.0`).
- Child modules inherit that version; bump it when behaviour or API contract changes in a way consumers should notice.

## Git tags

- Release markers use tags **`v` + version**, e.g. **`v0.1.0`** for Maven `0.1.0`.
- Pushing such a tag runs **`.github/workflows/release.yml`**, which builds Docker images tagged with the numeric version (and `release`) for reproducible demos. Pushing to a registry (GHCR, etc.) is optional and requires your own `docker login` / secrets — the workflow only proves the build on the tag.

## Branches

- **`main`**: keep CI green; this is the branch you show in a portfolio by default.
- **Demo / “broken on purpose”** work (old dependencies, intentional misconfigs for scanner screenshots) should live in a **separate branch** (e.g. `demo/scanner-baseline`) and **must not** be merged to `main`. Open a PR only as an artifact, or keep the branch for local runs.

## OAuth2 / larger features

- Treat **OAuth2/OIDC** (or similar) as a **feature branch off `main`**, not mixed with demo “bad” branches, so CI failures stay attributable to one change at a time.
