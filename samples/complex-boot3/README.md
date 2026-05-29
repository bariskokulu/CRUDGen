# complex-boot3

**Purpose:** Exercise **every** CRUDGen annotation option and generated endpoint type, with runtime tests on Spring Boot 3.

## Stack

- Spring Boot **3.2.9**
- MapStruct **1.5.5.Final**
- zjsonpatch (Jackson 2 path via Boot 3)
- H2 for JPA entities; Mongo autoconfig disabled in tests; `MongoTagRepository` mocked

MapStruct “unmapped target property” warnings during compile are expected (custom field names, ids not in DTOs).

## Entities and use cases

| Source | Demonstrates |
|--------|----------------|
| **MegaProduct** | JPA full stack; `packageName`; `repositoryName` / `serviceName` / `controllerName`; `extendRepo` / `extendService` / `extendController`; `Read`+`Create`+`Update`; `@FindBy` / `@FindAllBy`; `@DTOField(fieldName=…)`; security, lifecycle, OpenAPI, logging **on** |
| **ManualShelf** | `customService`; `Read`+`Create`; validation mirrored to DTO |
| **MongoTag** | `repo = MONGO`; full DTO set; `@FindBy` / `@FindAllBy` |
| **PlainCustomer** | `repo = PLAIN`; `service = true`; `customRepo`; no HTTP layer; security/lifecycle/OpenAPI/logging **off** |
| **BespokeItem** | `customController` (hand-written REST); generated service/repo/DTOs; `@FindBy` |
| **HeadlessTask** | `service = true`; no `controllerPath` — service + repo only |
| **FullHttpOps** | `@EndpointGen`; all five `HTTPMethod` values; security/logging/OpenAPI **on** |
| **EdgeOps** | `@EndpointGen` with `packageName`, `controllerName`; path variable + query param; security/logging/OpenAPI **off** |

Supporting types: `PlainCustomerRepository` + `PlainCustomerRepositoryImpl`, `ManualShelfService`, `BespokeItemController`, `MegaProduct*Ext` marker interfaces.

## Feature → test map

| Area | Test class |
|------|------------|
| Full CRUD, queries, batch POST/DELETE/PATCH | `MegaProductWebTest` |
| DTO field rename (`displayTitle`) | `MegaProductDtoTest` |
| `customService` + validation | `ManualShelfWebTest`, `ManualShelfDtoTest` |
| `RepoType.MONGO` HTTP | `MongoTagWebTest` (mock repository) |
| `RepoType.PLAIN` service | `PlainCustomerServiceTest` |
| `customController` | `BespokeItemWebTest`, `BespokeItemServiceTest` |
| Headless service | `HeadlessTaskServiceTest` |
| `@EndpointGen` HTTP matrix | `FullHttpOpsWebTest`, `EdgeOpsWebTest` |

Test wiring: `ComplexBoot3TestApplication` excludes `MongoTag` entity from component scan; imports `AllowAllSecurityService`, `NoopLifecycleCallbacks`, `MongoTagTestSupport` (`@Primary` mock `MongoTagRepository`).

19 tests.

## Commands

```bash
./gradlew :samples:complex-boot3:compileJava
./gradlew :samples:complex-boot3:test
```

## Boot 4 counterpart

Same feature matrix in [complex-boot4](../complex-boot4/) — separate sources, Boot 4 autoconfigure class names, Jackson 3.
