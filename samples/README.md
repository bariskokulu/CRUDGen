# Reference applications

Optional Gradle projects that demonstrate **`io.github.bariskokulu:crudgen`** at compile time and runtime. They are not part of the published JAR; copy patterns into your own app.

## Tiers

| Tier | Modules | Library meaning |
|------|---------|-----------------|
| **Minimal** | `simple-boot3`, `simple-boot4` | Smallest useful setup: one JPA entity, `Read`+`Create`, HTTP CRUD, security/lifecycle/OpenAPI/logging off |
| **Maximal** | `complex-boot3`, `complex-boot4` | Every processor option: all DTOs, PATCH batch, relations, queries, `RepoType` variants, `custom*` / `extend*`, `@EndpointGen` |

Boot 3 and Boot 4 pairs are the **same tier** on different stacks (Jackson 2 vs 3, autoconfigure package names). Sources are **not** shared between Boot versions.

## Which module to read

| Your stack | Minimal | Maximal |
|------------|---------|---------|
| Spring Boot 3 | `simple-boot3` | `complex-boot3` |
| Spring Boot 4 | `simple-boot4` | `complex-boot4` |

Human walkthrough: [docs/EXAMPLES.md](../docs/EXAMPLES.md).

## Per-module README

- [simple-boot3](simple-boot3/README.md) — minimal Boot 3
- [simple-boot4](simple-boot4/README.md) — minimal Boot 4
- [complex-boot3](complex-boot3/README.md) — maximal Boot 3
- [complex-boot4](complex-boot4/README.md) — maximal Boot 4

## Tests (approx.)

| Module | Tests |
|--------|-------|
| simple-boot3 / simple-boot4 | 7 each |
| complex-boot3 / complex-boot4 | 24 each |
