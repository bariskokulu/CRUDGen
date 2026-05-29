# simple-boot4

**Purpose:** Same minimal HTTP CRUD scenario as [simple-boot3](../simple-boot3/), running on the **Spring Boot 4** stack.

## Stack

- Spring Boot **4.0.5**
- `spring-boot-starter-webmvc` / `spring-boot-starter-webmvc-test`
- MapStruct **1.6.3**
- H2 in-memory database (tests only)
- Java **17** (`--release 17`)

## Source

One entity: `SimpleWidget` — identical **intent** to simple-boot3 (Read + Create, optional integrations off). This module owns its own copy of the file; nothing is imported from simple-boot3.

## Tests

| Class | Covers |
|-------|--------|
| `SimpleWidgetWebTest` | MockMvc CRUD smoke (uses `SimpleBoot4TestApplication`) |
| `SimpleWidgetDtoTest` | Validation on generated create DTO |

7 tests total.

## Commands

```bash
./gradlew :samples:simple-boot4:compileJava
./gradlew :samples:simple-boot4:test
```

## When to use this module

- Validate CRUDGen against Boot 4 / Jackson 3 before upgrading your app.
- Compare `build.gradle.kts` with simple-boot3 for dependency differences (`webmvc` vs `web`, MapStruct 1.6.x).

Full processor coverage: [complex-boot4](../complex-boot4/).
