# Versioning and Branch Strategy

## Version source of truth

- Project version is defined in root `pom.xml` using SemVer: `MAJOR.MINOR.PATCH`
- Current development line: `0.1.1`
- Child modules inherit the same version from the parent POM

## Tag and release format

- Git tag format: `vX.Y.Z` (example: `v0.1.0`)
- Tag value must match Maven project version without the `v` prefix
- Pushing a SemVer tag triggers `.github/workflows/release.yml`
- Release workflow verifies build/tests and creates version-tagged Docker images

## Branch model

- `main`
  - stable branch
  - CI is expected to stay green
- `feature/*`
  - active development branches (example: `feature/v0.1.1-hardening-tests`)
  - reviewed before merging into `main`
- `demo/*`
  - optional branch for intentionally insecure scanner demonstrations
  - never merged into `main`
