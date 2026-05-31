# Maximal usage — Spring Boot 3

Demonstrates **every** CrudGen annotation option and generated endpoint type the processor supports.

## Stack

- Spring Boot **3.2.9**, MapStruct **1.5.5.Final**, zjsonpatch (Jackson 2)
- H2 for JPA; Mongo autoconfig disabled in tests; `MongoTagRepository` mocked

MapStruct “unmapped target property” warnings at compile are expected (`fieldName`, ids omitted from DTOs).

## Coverage map

| Source | Library features |
|--------|------------------|
| **MegaProduct** | Full JPA HTTP stack; `packageName`; renamed repo/service/controller; `extend*`; all DTOs + PATCH/batch; `@FindBy` / `@FindAllBy`; `fieldName`; `relation` + `nestedRead`; security, lifecycle, OpenAPI, logging **on** |
| **ManualShelf** | `customService`; `Read`+`Create`; validation on DTO |
| **MongoTag** | `repo = MONGO`; full DTO set; field queries |
| **PlainCustomer** | `repo = PLAIN`; `service = true`; `customRepo`; no HTTP; cross-cutting **off** |
| **BespokeItem** | `customController`; `@FindBy`; security/lifecycle **off** on entity |
| **HeadlessTask** | `service = true`; no `controllerPath` |
| **FullHttpOps** | `@EndpointGen`; all `HTTPMethod` values |
| **EdgeOps** | `@EndpointGen` rename + path/query params; security/logging/OpenAPI **off** |

## Tests (24)

| Area | Class |
|------|-------|
| CRUD, queries, batch, relations, PATCH | `MegaProductWebTest` |
| Security + lifecycle invoked | `MegaProductHooksWebTest` |
| `fieldName` / Read DTO | `MegaProductDtoTest` |
| `customService` | `ManualShelfWebTest`, `ManualShelfDtoTest` |
| `MONGO` HTTP | `MongoTagWebTest` |
| `PLAIN` service | `PlainCustomerServiceTest` |
| `customController` | `BespokeItemWebTest`, `BespokeItemServiceTest` |
| Headless service | `HeadlessTaskServiceTest` |
| `@EndpointGen` | `FullHttpOpsWebTest`, `EdgeOpsWebTest` |

## Boot 4 counterpart

[complex-boot4](../complex-boot4/) — same **maximal** matrix; Jackson 3; Boot 4 autoconfigure class names.

## Docs

Checklist: [docs/EXAMPLES.md](../../docs/EXAMPLES.md) §11. Parameters: [docs/ANNOTATIONS.md](../../docs/ANNOTATIONS.md).
