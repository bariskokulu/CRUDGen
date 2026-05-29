# simple-boot3

**Purpose:** Show the smallest practical CRUDGen setup that exposes a working REST API and runtime tests.

## Stack

- Spring Boot **3.2.9**
- MapStruct **1.5.5.Final**
- H2 in-memory database (tests only)
- Java **17** (`--release 17`)

## Source

One entity: `SimpleWidget` (`com.example.crudgen.simple`).

| Setting | Value | Why |
|---------|-------|-----|
| `controllerPath` | `/api/widgets` | Enables service, controller, DTOs, mapper |
| `dtos` | `Read`, `Create` | GET/list/create; no JSON Patch dependency |
| `securityService` | `false` | No `CrudGenSecurityService` bean required in tests |
| `lifecycleHooks` | `false` | No lifecycle callback bean required |
| `openApi` / `logging` | `false` | Minimal generated surface |
| `repo` | default `JPA` | Standard Spring Data JPA repository |

`@DTOField` on `name` for both DTOs. `@NotBlank` / `@Size` on the entity are copied to `SimpleWidgetCreateDTO`.

## Generated artifacts (compile-time)

`SimpleWidgetRepository`, `SimpleWidgetService`, `SimpleWidgetController`, `SimpleWidgetReadDTO`, `SimpleWidgetCreateDTO`, `SimpleWidgetMapper`.

With `Read` DTO present, the controller also gets list, get-by-id, paged, and delete endpoints even though `Update` is omitted.

## Tests

| Class | Covers |
|-------|--------|
| `SimpleWidgetWebTest` | MockMvc: list, POST 201, GET by id, validation 400, 404, paged, DELETE 204 |
| `SimpleWidgetDtoTest` | Bean Validation on generated create DTO |

Test application: `SimpleBoot3TestApplication`. Profile: `test` (H2).

## Commands

```bash
./gradlew :samples:simple-boot3:compileJava
./gradlew :samples:simple-boot3:test
```

Windows: prefix with `gradlew.bat`.

## Boot 4 counterpart

Same scenario lives in [simple-boot4](../simple-boot4/) with Boot 4 dependencies and its own source copy.
