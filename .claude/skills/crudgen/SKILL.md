---
name: crudgen
description: >-
  CrudGen annotation processor (io.github.bariskokulu:crudgen) for Spring Boot 3/4.
  Use when annotating entities or use-case classes, configuring compile-time CRUD/REST
  generation, RepoType JPA/MONGO/PLAIN, DTOs, JSON Patch, @EndpointGen, security,
  or lifecycle hooks.
---

# CrudGen library

Compile-time annotation processor. You annotate persistence models or application classes; javac emits Spring-ready repositories, services, REST controllers, immutable DTOs, and MapStruct mappers. Artifact: `io.github.bariskokulu:crudgen:1.1.0`. Generated sources: `build/generated/sources/annotationProcessor/java/main/` (Gradle) or Maven equivalent.

**Public API packages**

- `com.bariskokulu.crudgen.annotation` — `@CrudGen`, `@EndpointGen`, `@Endpoint`, `@DTOField`
- `com.bariskokulu.crudgen.annotation.simple` — `@FindBy`, `@FindAllBy`
- `com.bariskokulu.crudgen.util.RepoType` — `JPA`, `MONGO`, `PLAIN`
- `com.bariskokulu.crudgen.util.HTTPMethod` — `GET`, `POST`, `PUT`, `PATCH`, `DELETE`

---

## Consumer build dependencies

| Need | When |
|------|------|
| `crudgen` on annotation-processor path | always |
| Lombok processor (before MapStruct) | if entities use Lombok |
| MapStruct + `mapstruct-processor` | any entity with non-empty `controllerPath` |
| Spring Web, Validation, Data JPA or Mongo | typical app; PLAIN-only may omit data store starter |
| Jackson + zjsonpatch | `"Update"` in `@CrudGen(dtos = …)` |

Processor order: **Lombok → MapStruct → CRUDGen**.

| Stack | MapStruct | zjsonpatch |
|-------|-----------|------------|
| Spring Boot 3 / Jackson 2 | 1.5.5.Final | `com.flipkart.zjsonpatch:zjsonpatch:0.4.x` |
| Spring Boot 4 / Jackson 3 | 1.6.3 | `io.github.vishwakarma:zjsonpatch:0.6.2+` |

Boot 4 web: `spring-boot-starter-webmvc` (not legacy `starter-web` naming in new apps).

Library JAR targets Java 8 bytecode; consumer app JDK is independent.

---

## @CrudGen — entity CRUD

**Target:** class (`@Entity`, `@Document`, etc.). **Requires** exactly one `@Id` field (Spring Data). ID type drives path variables and batch keys.

### Parameters (all optional unless noted)

| Parameter | Default | Effect |
|-----------|---------|--------|
| `repo` | `JPA` | `JPA` → `JpaRepository` + `JpaSpecificationExecutor`; `MONGO` → `MongoRepository`; `PLAIN` → explicit CRUD interface, no Spring Data supertype |
| `service` | `false` | Generate `{Entity}Service` when `true` **or** when `controllerPath` is set |
| `controllerPath` | `""` | Base REST path; **must start with `/`**. Empty → no controller, DTOs, or mapper |
| `dtos` | `{}` | `"Read"`, `"Create"`, `"Update"`. If `controllerPath` set → **`Read` required** |
| `repositoryName`, `serviceName`, `controllerName` | `{Entity}*` | Rename generated types |
| `packageName` | entity package | Output package for generated types |
| `customRepo` | — | No generated repository; use given `@Repository` class |
| `customService` | — | No generated service; controller injects this type when HTTP exists |
| `customController` | — | No generated controller; service/repo/DTOs/mapper still follow rules below |
| `extendRepo`, `extendService`, `extendController` | — | Generated type extends/implements your interface |
| `securityService` | `true` | Generated controllers call `CrudGenSecurityService` |
| `lifecycleHooks` | `true` | Inject `EntityLifecycleCallbacks<T>` into generated controller/service |
| `logging` | `true` | SLF4J debug logs in generated code |
| `openApi` | `true` | Swagger v3 annotations on generated controllers (app still needs springdoc for UI) |

### What gets generated

```
Repository     always (unless customRepo)
Service        if service=true OR controllerPath non-empty (unless customService)
Controller     if controllerPath non-empty (unless customController)
DTOs + Mapper  if controllerPath non-empty AND Read in dtos AND @DTOField on fields
PATCH support  if Update in dtos (requires zjsonpatch + Jackson on compile classpath)
```

### REST surface (generated controller)

Base = `controllerPath`. All responses use Read DTO unless noted.

| HTTP | Path | Notes |
|------|------|-------|
| GET | `/{id}` | 404 if missing |
| GET | `/` | all entities |
| GET | `/paged?page=&size=` | page ≥ 0, size 1–500 |
| POST | `/` | Create DTO body → **201 Created** |
| POST | `/batch` | Create DTO list, max 500 → **201** |
| PATCH | `/{id}` | `Content-Type: application/json-patch+json`; patches **Update DTO JSON shape** (RFC 6902) |
| PATCH | `/batch` | JSON map `{ "id": [patch ops…], … }` → **200** |
| DELETE | `/{id}` | **204** |
| DELETE | `/batch` | JSON id list → **204** |
| GET | `/findBy{Field}?{field}=` | from `@FindBy`; **404** if not found |
| GET | `/findAllBy{Field}?{field}=` | from `@FindAllBy` |
| GET | `/findAllBy{Field}/paged?…` | paginated `@FindAllBy` |

Generated controller: `@RestController`, `@Validated`, `@RequestMapping(controllerPath)`. Create uses `@Valid` on body; PATCH runs `validator.validate` after merge.

### RepoType.PLAIN

Generated `{Entity}Repository` is a `@Repository` **interface** declaring: `findById`, `findAll`, `findAll(Pageable)`, `save`, `saveAll`, `deleteById`, `deleteAllById`, plus `@FindBy` / `@FindAllBy` methods. **You** implement it (JDBC, jOOQ, MyBatis, R2DBC, etc.). Pagination types: Spring `Page` / `Pageable` from `spring-data-commons`.

With `customRepo`, the processor skips generating the interface and uses your implementation class.

---

## @DTOField

**Target:** entity field. **Repeatable.**

| Attribute | Meaning |
|-----------|---------|
| `dto` | `"Read"`, `"Create"`, or `"Update"` |
| `fieldName` | DTO property name; default = entity field name |

Only annotated fields appear on that DTO. Bean Validation (`javax.*` / `jakarta.*`) on entity fields is **copied** to generated DTO fields. JPA/Mongo mapping annotations are not copied.

---

## @FindBy / @FindAllBy

**Target:** entity field. No attributes.

- `@FindBy` → `findBy{Field}` in repo/service; GET endpoint as above; single result, 404 when null.
- `@FindAllBy` → list + paged variants in repo/service/controller.

---

## @EndpointGen + @Endpoint — use-case HTTP

For **your** `@Component` / service class — not entity CRUD.

**@EndpointGen** (class):

| Parameter | Default |
|-----------|---------|
| `controllerPath` | **required**, starts with `/` |
| `controllerName` | `{Class}Controller` |
| `packageName` | class package |
| `securityService`, `logging`, `openApi` | `true` |

**@Endpoint** (method): `method` = `HTTPMethod.*`, `path` = relative segment starting with `/`.

Rules:

- Each `{name}` in path must match a method parameter name → `@PathVariable` (use explicit `@PathVariable("name")` if compiler lacks `-parameters`).
- Existing `@RequestBody`, `@RequestParam`, etc. are preserved.
- Non-void return → `ResponseEntity.ok(…)`; void → **204 No Content**.
- No `lifecycleHooks` on use-case controllers.

Generated class is HTTP adapter; **your class remains the implementation**.

---

## Shared interfaces (app implements)

Emitted **once per compilation** when any annotated type needs them:

### `CrudGenSecurityService` (`com.bariskokulu.crudgen.security`)

When any `@CrudGen` or `@EndpointGen` has `securityService = true` (default):

```java
void checkEntityAccess(String entityClassName, String method, Object... params);
void checkUseAccess(String method, Object... params);
```

- Entity: `entityClassName` = entity FQCN; `method` = generated REST method name (`get`, `create`, `findByEmail`, …).
- Use-case: `method` = `com.example.JobApi#run`.

If **all** types set `securityService = false`, interface is **not** generated.

### `EntityLifecycleCallbacks<T>` (`com.bariskokulu.crudgen.lifecycle`)

When any entity has `lifecycleHooks = true` (default). Injected `@Nullable` into generated controller and service.

Implement: `beforeCreate`, `afterCreate`, `beforeUpdate`, `afterUpdate`, `beforeDelete`, `afterDelete`.

Optional defaults (override for batch): `beforeCreateBatch`, `afterCreateBatch`, `beforeUpdateBatch`, `afterUpdateBatch`, `beforeDeleteBatch`, `afterDeleteBatch`.

If **all** entities set `lifecycleHooks = false`, interface is **not** generated.

---

## MapStruct mapper (when Read DTO exists)

- `get(Entity)` → Read DTO
- `create(CreateDTO)` → Entity
- `toPatch(Entity)` → Update DTO
- `patch(Entity, UpdateDTO)` — merge helper

Unmapped-property warnings are common with `fieldName` renames or ids omitted from DTOs.

---

## Agent workflow

1. Decide layers: repo-only, service-only, or full HTTP (`controllerPath` + `dtos`).
2. Mark `@Id` and `@DTOField` per DTO type needed.
3. Add `@FindBy` / `@FindAllBy` only for query fields you want exposed.
4. Set `securityService` / `lifecycleHooks` to `false` until app beans exist, or scaffold implementations.
5. After compile, read generated sources — **do not hand-write duplicate REST/service methods** for generated surface.
6. PATCH clients: JSON Patch against Update DTO field names, not entity field names when `fieldName` differs.

---

## Common mistakes

| Mistake | Fix |
|---------|-----|
| `controllerPath` without `"Read"` in `dtos` | add Read + `@DTOField(dto="Read")` |
| `Update` without zjsonpatch | add dep or remove Update |
| Missing `CrudGenSecurityService` bean | implement or `securityService=false` everywhere |
| `@PathVariable` without name | `@PathVariable("id")` or `-parameters` |
| `@Endpoint` path `{id}` but param named differently | names must match |
| Duplicate custom + generated controller | `customController` replaces generated HTTP entirely for that entity |

---

## Maintainer note (this repo only)

Changing processor behavior: run `gradlew verifyAllExamples` after `lib` edits. Sample modules under `samples/` are regression fixtures, not part of the published library API.

Human docs in repo: `docs/ANNOTATIONS.md`, `docs/DECISION-TREE.md`, `docs/EXAMPLES.md`, `docs/TROUBLESHOOTING.md`.
