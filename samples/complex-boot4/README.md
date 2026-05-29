# complex-boot4

**Purpose:** Same full CRUDGen matrix as [complex-boot3](../complex-boot3/), validated on **Spring Boot 4**.

## Stack

- Spring Boot **4.0.5**
- `spring-boot-starter-webmvc` / `spring-boot-starter-webmvc-test`
- MapStruct **1.6.3**
- **zjsonpatch 0.6.2** (Jackson 3)
- H2 + mocked Mongo (no real MongoDB in tests)

This module is **standalone**: its `src/main` and `src/test` trees are not linked to complex-boot3.

## Boot 4 specifics (tests)

Mongo must not start in the H2 test context. `ComplexBoot4TestApplication` excludes:

- `org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration`
- `org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration`
- `org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration`

`application-test.properties` repeats the same excludes and sets `spring.data.mongodb.repositories.enabled=false`. `MongoTag` is filtered from entity scan; `MongoTagTestSupport` supplies a `@Primary` mock `MongoTagRepository`.

## Entities and tests

Same catalog as complex-boot3 — see [complex-boot3/README.md](../complex-boot3/README.md) for the entity → feature table.

| Tests | Count |
|-------|-------|
| All complex-boot3 test classes (Boot 4 copies) | 19 |

Notable classes: `MegaProductWebTest` (includes `PATCH /batch`), `MongoTagWebTest` (create, patch, findBy, findAllBy), `BespokeItemWebTest` (custom controller + generated service).

## Commands

```bash
./gradlew :samples:complex-boot4:compileJava
./gradlew :samples:complex-boot4:test
```

## When to use this module

- Regression-test CRUDGen after Boot 4 / Jackson 3 upgrades.
- Copy test patterns for excluding Mongo autoconfig while keeping a `@CrudGen(repo = MONGO)` entity on the classpath.

Minimal sample: [simple-boot4](../simple-boot4/).
