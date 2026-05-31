# Minimal usage — Spring Boot 3

Demonstrates the **smallest** practical CrudGen setup: one JPA entity, HTTP JSON API, no JSON Patch, no security/lifecycle beans required.

## Stack

- Spring Boot **3.2.9**, MapStruct **1.5.5.Final**
- Default `repo = JPA`, H2 in tests

## Entity: `SimpleWidget`

| `@CrudGen` | Value | Why |
|------------|-------|-----|
| `controllerPath` | `/api/widgets` | Enables service, controller, DTOs, mapper |
| `dtos` | `Read`, `Create` | List/get/create; no zjsonpatch |
| `securityService` | `false` | No `CrudGenSecurityService` bean yet |
| `lifecycleHooks` | `false` | No lifecycle bean yet |
| `openApi` / `logging` | `false` | Minimal generated surface |

`@DTOField` on `name` for both DTOs; `@NotBlank` / `@Size` copied to create DTO.

## Generated at compile time

`SimpleWidgetRepository`, `SimpleWidgetService`, `SimpleWidgetController`, `SimpleWidgetReadDTO`, `SimpleWidgetCreateDTO`, `SimpleWidgetMapper`.

With `Read` present, controller also exposes list, get-by-id, paged, batch create/delete (no PATCH without `Update`).

## Tests

`SimpleWidgetWebTest` (MockMvc CRUD), `SimpleWidgetDtoTest` (validation on generated DTO).

## Boot 4 counterpart

[simple-boot4](../simple-boot4/) — same **minimal** tier, Boot 4 / Jackson 3 / `spring-boot-starter-webmvc`.

## Docs

Walkthrough: [docs/EXAMPLES.md](../../docs/EXAMPLES.md) §1.
