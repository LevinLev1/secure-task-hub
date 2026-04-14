# Versioning and Branch Strategy

## Version source of truth

- Project version is defined in root `pom.xml` using SemVer: `MAJOR.MINOR.PATCH`
- Current development line: `0.1.0`
- Child modules inherit the same version from the parent POM

## Tag and release format

- Git tag format: `vX.Y.Z` (example: `v0.1.0`)
- Tag value must match Maven project version without the `v` prefix
- Pushing a SemVer tag triggers `.github/workflows/release.yml`
- Release workflow verifies build/tests and creates version-tagged Docker images

## Branch model

- `main`
  - portfolio-ready branch
  - always expected to keep CI green
  - no intentionally vulnerable or broken commits
- `feature/*`
  - normal development branches (example: `feature/oauth2`)
  - merge to `main` via pull request
- `demo/*`
  - intentionally insecure or scanner-failing examples for demonstrations
  - isolated from `main`
  - can be used for screenshots and interview walkthroughs
