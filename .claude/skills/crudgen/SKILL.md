---
name: crudgen
description: >-
  CrudGen (io.github.bariskokulu:crudgen) compile-time processor for Spring Boot 3/4.
  Use for @CrudGen entity CRUD, @DTOField relations, JSON Patch Update DTOs, RepoType
  JPA/MONGO/PLAIN, @FindBy/@FindAllBy, @EndpointGen use-case HTTP, security and lifecycle hooks.
---

# CrudGen

CrudGen is a **compile-time annotation processor**. You annotate persistence models or application classes; javac emits Spring-ready types (repositories, services, REST adapters, immutable DTOs, MapStruct mappers, relation appliers).

**Artifact:** `io.github.bariskokulu:crudgen:1.1.0`  
**Bytecode:** Java 8 (consumer app JDK and Spring Boot version are independent).

**Public API**

| Package | Types |
|---------|--------|
| `com.bariskokulu.crudgen.annotation` | `@CrudGen`, `@EndpointGen`, `@Endpoint`, `@DTOField` |
| `com.bariskokulu.crudgen.annotation.simple` | `@FindBy`, `@FindAllBy` |
| `com.bariskokulu.crudgen.util` | `RepoType`, `HTTPMethod` |

**Generated helper interfaces** (once per compilation when needed)

| Interface | Package | When emitted |
|-----------|---------|--------------|
| `CrudGenSecurityService` | `com.bariskokulu.crudgen.security` | Any `@CrudGen` / `@EndpointGen` with `securityService = true` (default) |
| `EntityLifecycleCallbacks<T>` | `com.bariskokulu.crudgen.lifecycle` | Any `@CrudGen` with `lifecycleHooks = true` (default) |

---

## Install

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    compileOnly("io.github.bariskokulu:crudgen:1.1.0")
    annotationProcessor("org.projectlombok:lombok:1.18.42")              // if Lombok on entities
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final") // Boot 4: 1.6.3
    annotationProcessor("io.github.bariskokulu:crudgen:1.1.0")
}
```

### Maven

`provided` dependency + `annotationProcessorPaths` in this order: **Lombok → MapStruct → CrudGen**.

### Extra dependencies

| Need | Condition |
|------|-----------|
| Spring Web MVC + Validation | HTTP layer |
| Spring Data JPA or Mongo | `repo = JPA` or `MONGO` |
| Your repository implementation | `repo = PLAIN` or `customRepo` |
| MapStruct processor | Non-empty `controllerPath` (DTOs + mapper) |
| Jackson databind + zjsonpatch | `"Update"` in `@CrudGen(dtos = …)` |

| Stack | MapStruct | zjsonpatch |
|-------|-----------|------------|
| Spring Boot 3 / Jackson 2 | 1.5.5.Final | `com.flipkart.zjsonpatch:zjsonpatch:0.4.16` |
| Spring Boot 4 / Jackson 3 | 1.6.3 | `io.github.vishwakarma:zjsonpatch:0.6.2` |

Boot 4 web: prefer `spring-boot-starter-webmvc`.

Processor order is always **Lombok → MapStruct → CrudGen**.

---

## Usage profiles

| Profile | Goal | Typical annotations |
|---------|------|---------------------|
| **Minimal** | Smallest working REST API | `controllerPath`, `dtos = {Read, Create}`, `securityService = false`, `lifecycleHooks = false`, default `repo = JPA` |
| **Full** | Every library feature in one app | All `dtos`, relations, batch PATCH, `@FindBy`/`@FindAllBy`, `extend*`, `custom*`, `RepoType` variants, `@EndpointGen`, security + lifecycle on |

See `docs/EXAMPLES.md` for progressive recipes (minimal → full). Parameter tables: `docs/ANNOTATIONS.md`. Generation rules: `docs/DECISION-TREE.md`.

---

## Capability map (everything the processor can generate)

### Entity layer (`@CrudGen`)

| Capability | How you enable it |
|------------|-------------------|
| Spring Data JPA repository | `repo = JPA` (default) |
| Spring Data Mongo repository | `repo = MONGO` |
| Plain persistence contract (you implement) | `repo = PLAIN` |
| Skip generated repository | `customRepo = YourRepo.class` (your bean; no Jpa/Mongo extend required) |
| Generated service | `service = true` or non-empty `controllerPath` |
| Skip generated service | `customService = YourService.class` |
| REST controller + DTOs + MapStruct mapper | `controllerPath = "/api/…"` + `dtos` + `@DTOField` |
| Skip generated controller | `customController = YourController.class` |
| Rename generated types / output package | `repositoryName`, `serviceName`, `controllerName`, `packageName` |
| Extend generated types | `extendRepo`, `extendService`, `extendController` |
| Immutable JSON DTOs (`Read` / `Create` / `Update`) | `dtos` + `@DTOField` per field |
| Bean Validation on DTOs | Validation annotations on entity fields (copied) |
| FK relations in DTO as ids | `@DTOField(relation = true)` |
| Flatten related Read fields | `@DTOField(relation = true, nestedRead = true)` on scalar ref |
| `{Entity}RelationApplier` component | Auto when relations exist and generated controller is used |
| Field query: single result | `@FindBy` |
| Field query: list + paged | `@FindAllBy` |
| JSON Patch update | `"Update"` in `dtos` + zjsonpatch on compile classpath |
| OpenAPI v3 on controller | `openApi = true` (default) |
| SLF4J debug logging | `logging = true` (default) |
| Security gate on each endpoint | `securityService = true` (default) |
| Lifecycle hooks at REST boundary | `lifecycleHooks = true` (default) |

### Use-case layer (`@EndpointGen` + `@Endpoint`)

| Capability | How you enable it |
|------------|-------------------|
| HTTP adapter for your `@Component` class | `@EndpointGen(controllerPath = "/api/…")` |
| Map `HTTPMethod` + path to Spring mapping | `@Endpoint` on methods |
| Preserve your `@RequestBody` / `@RequestParam` | Existing Spring annotations kept |
| OpenAPI / logging / security | Same flags as `@EndpointGen` |

### Generated HTTP surface (entity controller)

Base path = `controllerPath`. Responses use **Read DTO** unless noted.

| HTTP | Path | Body / params | Response |
|------|------|---------------|----------|
| GET | `/{id}` | path `id` | 200 or **404** |
| GET | `/` | — | 200 list |
| GET | `/paged` | `page`, `size` (required) | 200 `Page` |
| POST | `/` | Create DTO, `@Valid` | **201** |
| POST | `/batch` | Create DTO list, max **500** | **201** |
| PATCH | `/{id}` | JSON Patch array, `Content-Type: application/json-patch+json` | 200 or **400** / **404** |
| PATCH | `/batch` | Map `{ "id": [patch ops…] }`, max **500** keys | 200 or **400** / **404** |
| DELETE | `/{id}` | — | **204** |
| DELETE | `/batch` | id list, max **500** | **204** |
| GET | `/findBy{Field}` | query param = field name | 200 or **404** if null |
| GET | `/findAllBy{Field}` | query param | 200 list |
| GET | `/findAllBy{Field}/paged` | query + `page` + `size` | 200 `Page` |

PATCH merges into the **Update DTO shape** (RFC 6902), then validates, maps onto entity, runs relation applier, saves.

---

## @CrudGen

**Target:** class with exactly one `@Id` field (`jakarta.persistence.Id`, `javax.persistence.Id`, or `org.springframework.data.annotation.Id`).

| Parameter | Default | Effect |
|-----------|---------|--------|
| `repo` | `JPA` | `JPA` / `MONGO` / `PLAIN` (see below) |
| `service` | `false` | Generate `{Entity}Service` when `true` **or** `controllerPath` non-empty |
| `controllerPath` | `""` | Must start with `/`. Empty → no controller, DTOs, or mapper |
| `dtos` | `{}` | Only `Read`, `Create`, `Update`. With `controllerPath` → **Read required** |
| `packageName` | entity package | Output package for generated types |
| `repositoryName`, `serviceName`, `controllerName` | `{Entity}*` | Rename generated types |
| `customRepo` | — | No generated repository; service injects your type. Must **not** extend `JpaRepository`/`MongoRepository` unless you choose to — must expose the same methods as **PLAIN** (below) |
| `customService` | — | No generated service; controller injects this type |
| `customController` | — | No generated controller; **incompatible** with `@DTOField(relation=true)` unless you call `{Entity}RelationApplier` yourself |
| `extendRepo` | — | Generated repo **only** extends this type (must subtype `JpaRepository` / `MongoRepository` for JPA/MONGO) |
| `extendService`, `extendController` | — | Generated type implements your interface |
| `securityService` | `true` | Calls `CrudGenSecurityService` from generated controllers |
| `lifecycleHooks` | `true` | Injects `EntityLifecycleCallbacks<T>` into **generated controller only** |
| `logging`, `openApi` | `true` | SLF4J debug / Swagger `@Operation` etc. |

### RepoType

**JPA** — generated interface extends `JpaRepository<E,ID>` + `JpaSpecificationExecutor<E>` (unless `extendRepo` replaces base).

**MONGO** — extends `MongoRepository<E,ID>` (unless `extendRepo`).

**PLAIN** — `@Repository` interface with `findById`, `findAll`, `findAll(Pageable)`, `save`, `saveAll`, `deleteById`, `deleteAllById`, plus `@FindBy` / `@FindAllBy` methods. **You** provide the implementation.

**`customRepo`** — same method contract as PLAIN on **your** class or interface (Spring bean). Processor does not check Spring Data supertypes. `repo` is ignored for generation when `customRepo` is set; prefer `repo = PLAIN` for clarity. Relation applier calls `findById` on related entities’ `customRepo` types too.

---

## @DTOField

**Target:** entity field. **Repeatable.**

| Attribute | Default | Effect |
|-----------|---------|--------|
| `dto` | — | `Read`, `Create`, or `Update` |
| `fieldName` | entity name | DTO property name (API/JSON name) |
| `relation` | `false` | `true`: scalar → `{field}Id`; collection → `{field}Ids` as `List<Long>` (element type needs `@Id`) |
| `nestedRead` | `false` | With `relation=true` on scalar ref, Read DTO also gets prefixed scalars from related entity’s Read `@DTOField`s |

Only fields with `@DTOField` for that DTO appear on the DTO. Empty DTO name in `dtos` with no fields → compile error.

**Relation flow (Create / Update / PATCH):** MapStruct ignores relation entity fields on write; `{Entity}RelationApplier` loads related rows via `findById` (deduped ids), sets managed references, then `save`. Missing FK → **400**. Related type must be `@CrudGen`.

**`relation = false`:** DTO keeps entity field type (`List<Line>`, embeddable, etc.) — you handle serialization/lazy loading.

---

## @FindBy / @FindAllBy

No attributes. Generate repository + service + HTTP:

- `@FindBy` → `findBy{Field}`; HTTP returns **404** when null (not when multiple DB rows — that is a persistence exception).
- `@FindAllBy` → list + `findAllBy{Field}Paged` + matching GET endpoints.

---

## @EndpointGen + @Endpoint

**@EndpointGen** on a class (your implementation stays the bean):

| Parameter | Default |
|-----------|---------|
| `controllerPath` | **required**, starts with `/` |
| `controllerName`, `packageName` | optional rename |
| `securityService`, `logging`, `openApi` | `true` |

**@Endpoint** on methods: `method = HTTPMethod.*`, `path` starts with `/`.

- Each `{token}` in path must match a Java parameter name (or use `@PathVariable("token")`).
- Non-void → `ResponseEntity.ok(body)`; void → **204**.
- No entity lifecycle hooks on use-case controllers.

Security key: `checkUseAccess("com.example.Ops#methodName", …)`.

---

## CrudGenSecurityService

```java
void checkEntityAccess(String entityClassName, String method, Object... params);
void checkUseAccess(String useCaseKey, Object... params);
```

- Entity: `entityClassName` = entity FQCN; `method` = generated method name (`get`, `create`, `findBySku`, …).
- Use-case: `useCaseKey` = `com.example.JobApi#run`.

If **every** `@CrudGen` and `@EndpointGen` sets `securityService = false`, the interface is **not** generated.

---

## EntityLifecycleCallbacks&lt;T&gt;

Hooks run on the **generated REST controller** only (not on generated service `delete`).

Implement: `beforeCreate`, `afterCreate`, `beforeUpdate`, `afterUpdate`, `beforeDelete`, `afterDelete`.

Optional batch defaults: `beforeCreateBatch`, `afterCreateBatch`, `beforeUpdateBatch(List<T>)`, `afterUpdateBatch`, `beforeDeleteBatch`, `afterDeleteBatch`.

**Batch PATCH order:** `beforeUpdateBatch` → per item `beforeUpdate` → patch + relation applier → `saveAll` (one transaction) → per item `afterUpdate` → `afterUpdateBatch`.

If **every** entity sets `lifecycleHooks = false`, interface not generated.

---

## MapStruct mapper (when Read DTO exists)

| Method | Role |
|--------|------|
| `get(Entity)` | Entity → Read DTO |
| `create(CreateDTO)` | Create DTO → Entity |
| `toPatch(Entity)` | Entity → Update DTO (patch baseline) |
| `patch(Entity, UpdateDTO)` | Merge update DTO onto entity (`SET_TO_NULL`) |

Relation fields ignored on create/patch mapping; applier sets them after mapping.

Unmapped-property warnings are normal when `id` is omitted from DTOs or `fieldName` renames entity columns.

---

## What CrudGen does not generate

| Topic | Notes |
|-------|--------|
| Merge Patch (RFC 7396) | Only JSON Patch (RFC 6902) on Update DTO |
| JPA join-table business rules | Orphan removal, “role already on other user”, etc. — your service layer |
| OpenAPI schemas per DTO field | Annotations on operations; not full `@Schema` on every property |
| `findBy` with multiple DB matches | Spring Data may throw; not converted to 409 |
| Application code inside your `@Component` | Processor only adds HTTP adapter for `@EndpointGen` |

---

## Agent workflow

1. Choose profile: minimal REST vs full stack (see `docs/EXAMPLES.md`).
2. Put `@Id` on entity; add `@DTOField` for each DTO type you list in `dtos`.
3. Add `"Update"` only if zjsonpatch is on the compile classpath.
4. Set `securityService` / `lifecycleHooks` to `false` until app beans exist, or implement the generated interfaces.
5. After compile, read `build/generated/sources/annotationProcessor/…` — do not duplicate generated endpoints by hand.
6. PATCH clients target **Update DTO property names** (after `fieldName`), not internal entity field names.

---

## Common mistakes

| Mistake | Fix |
|---------|-----|
| `controllerPath` without `Read` in `dtos` | Add `Read` + at least one `@DTOField(dto="Read")` |
| `Update` without zjsonpatch | Add dependency or remove `Update` |
| No `CrudGenSecurityService` bean | Implement or `securityService=false` on all annotated types |
| `customController` + `relation=true` | Use generated controller or wire `{Entity}RelationApplier` manually |
| `extendRepo` that is not a Jpa/Mongo repository | Use marker interface that extends `JpaRepository<E,ID>` etc. |
| `@PathVariable` without name | `@PathVariable("id")` or `-parameters` |
| PATCH wrong property path | Patch `/displayTitle` if DTO uses `fieldName = "displayTitle"` |

---

## Related docs

| File | Content |
|------|---------|
| `docs/EXAMPLES.md` | Minimal → full usage recipes with code |
| `docs/ANNOTATIONS.md` | Parameter reference |
| `docs/DECISION-TREE.md` | Generation decision tree |
| `docs/TROUBLESHOOTING.md` | Build and runtime failures |

Human docs and both agent skills (`.cursor/skills/crudgen/SKILL.md`, `.claude/skills/crudgen/SKILL.md`) must stay in sync when the processor changes.
