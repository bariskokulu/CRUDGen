# What gets generated (decision tree)

## `@CrudGen` on an entity class

| Your annotation | Result |
|-----------------|--------|
| `@CrudGen` only (default `service = false`, empty `controllerPath`) | **Repository** only (`*Repository`). |
| `repo = RepoType.PLAIN` | **Repository** is a Spring `@Repository` interface with explicit CRUD + `Page`/`Pageable` methods (no `JpaRepository` / `MongoRepository`). You implement it with JDBC, jOOQ, MyBatis, R2DBC, etc. Still uses Spring’s `Page` / `Pageable` types so the generated **service** and **controller** stay unchanged. |
| `repo = PLAIN` and `extendRepo` set | Only **extends** your type; no extra CRUD methods are generated—you must inherit or declare every method the generated **service** calls (`findById`, `save`, …). |
| `repo = RepoType.JPA` / `MONGO` | Repository extends Spring Data **JPA** or **MongoDB** as before. |
| `@CrudGen(service = true)` and empty `controllerPath` | **Repository** + **Service**. |
| `controllerPath = "/api/..."` | **Repository**, **Service**, **Mapper**, **DTOs** (from `dtos` + `@DTOField`), **Controller**. Requires a **Read** DTO in `dtos`. |
| `customRepo` / `customService` / `customController` | Skips generating that layer; use your type instead. |
| `extendRepo` / `extendService` / `extendController` | Generated type **extends** or **implements** that type (see generator for exact superinterface rules). |

### Flags that change behavior

- **`securityService`** (default `true`): generated controllers call `CrudGenSecurityService`. The **`CrudGenSecurityService` interface stub is emitted only if** at least one `@CrudGen` or `@EndpointGen` type has `securityService = true`.
- **`lifecycleHooks`** (default `true`): service/controller call `EntityLifecycleCallbacks<T>` (nullable bean). Implement the six abstract methods; `*Batch` methods are optional `default` no-ops. The interface is emitted only if at least one `@CrudGen` has `lifecycleHooks = true`.
- **`openApi`**: adds Swagger/OpenAPI 3 annotations on generated controllers when `true`.
- **`logging`**: adds `logger.debug(...)` in generated controller/service when `true`.

### Field annotations

- **`@DTOField(dto = "Read")`** (repeatable): includes that field in the matching `*ReadDTO`, `*CreateDTO`, etc.
- **`@FindBy`**: repository `findBy*`, service, and controller endpoint.
- **`@FindAllBy`**: list + paged variants on repository, service, and controller.

## `@EndpointGen` + `@Endpoint` on a class

| Condition | Result |
|-----------|--------|
| Class has `@EndpointGen(controllerPath = "...")` and methods with `@Endpoint` | **`*Controller`** exposing Spring MVC mappings that delegate to your class (the “use case” service bean). |

## After you turn off security or lifecycle

If you set `securityService = false` everywhere (or `lifecycleHooks = false` everywhere), run a **clean compile** so old generated stubs under `build/generated/...` are not confused with your new settings. The processor does not delete stale files.

## Bean Validation package

The processor chooses **`javax.*` vs `jakarta.*`** from the compile classpath: if only **`javax.validation.Validator`** is present, generated code uses **`javax`**; otherwise it defaults to **`jakarta`**. If both are visible, prefer a single validation API on the annotation processor classpath to avoid ambiguity.
