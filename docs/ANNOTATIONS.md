# Annotation reference

**Artifact:** `io.github.bariskokulu:crudgen`

Public API:

- `com.bariskokulu.crudgen.annotation` — `@CrudGen`, `@EndpointGen`, `@Endpoint`, `@DTOField`
- `com.bariskokulu.crudgen.annotation.simple` — `@FindBy`, `@FindAllBy`
- `com.bariskokulu.crudgen.util.RepoType` — `JPA`, `MONGO`, `PLAIN`

---

## @CrudGen

**Target:** class (entity or document type)

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `repo` | `RepoType` | `JPA` | `JPA`, `MONGO`, or `PLAIN` |
| `service` | `boolean` | `false` | Generate service. Also generated when `controllerPath` is set (unless `customService`) |
| `controllerPath` | `String` | `""` | Base REST path, e.g. `/api/items`. Must start with `/`. Empty → no controller/DTOs/mapper |
| `dtos` | `String[]` | `{}` | `"Read"`, `"Create"`, `"Update"`. `Read` required when `controllerPath` is set |
| `controllerName` | `String` | `""` | Default `{Entity}Controller` |
| `serviceName` | `String` | `""` | Default `{Entity}Service` |
| `repositoryName` | `String` | `""` | Default `{Entity}Repository` |
| `packageName` | `String` | `""` | Output package; default = entity package |
| `customRepo` | `Class<?>` | `Void` | Skip generated repository; generated service injects this type. **Does not** need to extend `JpaRepository`/`MongoRepository`. Must expose the PLAIN method contract below (register as a Spring bean) |
| `customController` | `Class<?>` | `Void` | Skip generated controller. Incompatible with `@DTOField(relation=true)` unless you call `{Entity}RelationApplier` yourself |
| `customService` | `Class<?>` | `Void` | Skip generated service |
| `extendRepo` | `Class<?>` | `Void` | Generated repository **only** extends this type (no extra `JpaRepository`/`MongoRepository`/`PLAIN` methods). `extendRepo` must subtype the `repo` contract (e.g. `JpaRepository<E,ID>` for `JPA`) |
| `extendService` | `Class<?>` | `Void` | Generated service implements this interface |
| `extendController` | `Class<?>` | `Void` | Generated controller implements this interface |
| `securityService` | `boolean` | `true` | Call `CrudGenSecurityService` from generated controllers |
| `logging` | `boolean` | `true` | SLF4J debug logs in generated code |
| `openApi` | `boolean` | `true` | Swagger v3 annotations on generated controllers |
| `lifecycleHooks` | `boolean` | `true` | Inject `EntityLifecycleCallbacks<T>` into generated controller (REST boundary only) |

### ID field

Exactly one field annotated with `@Id` (Spring Data). Its type is the ID type everywhere (path variables, batch keys). Missing `@Id` → compile error.

### Generated controller endpoints

When `controllerPath` is set and no `customController`:

| Method | Path | Condition |
|--------|------|-----------|
| GET | `/{id}` | always (Read DTO) |
| GET | `/` | list |
| GET | `/paged` | pagination (`page`, `size`) |
| POST | `/` | Create DTO → **201 Created** |
| POST | `/batch` | Create DTO list → **201** |
| PATCH | `/{id}` | Update DTO + JSON Patch body |
| PATCH | `/batch` | Map of id → patch |
| DELETE | `/{id}` | **204** |
| DELETE | `/batch` | id list → **204** |
| GET | `/findBy{Field}` | `@FindBy` fields; **404** if not found |
| GET | `/findAllBy{Field}` | `@FindAllBy` |
| GET | `/findAllBy{Field}/paged` | `@FindAllBy` + pagination |

Batch bodies: max **500** items (`@Size` on generated parameters).

### Repository shapes

- **JPA** — `JpaRepository<E, ID>` + `JpaSpecificationExecutor<E>`
- **MONGO** — `MongoRepository<E, ID>`
- **PLAIN** — `@Repository` interface with explicit `findById`, `findAll`, `findAll(Pageable)`, `save`, `saveAll`, `deleteById`, `deleteAllById`, plus field queries

**`customRepo`** — no generated repository interface. Your class or interface implements the same operations as PLAIN (`findById` returns `Optional<E>`, plus any `@FindBy` / `@FindAllBy` methods). JDBC, MyBatis, in-memory, etc. are fine. `extendRepo` rules apply only when the processor **generates** a repository (no `customRepo`). `{Entity}RelationApplier` injects related entities’ `customRepo` types for `findById`.

---

## @EndpointGen

**Target:** class (typically a `@Component` use-case)

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `controllerPath` | `String` | — | **Required.** Base path |
| `controllerName` | `String` | `""` | Default `{Class}Controller` |
| `packageName` | `String` | `""` | Output package |
| `securityService` | `boolean` | `true` | Use-case security checks |
| `logging` | `boolean` | `true` | Debug logging |
| `openApi` | `boolean` | `true` | OpenAPI annotations |

Generated type: `@RestController` + `@Validated`, delegating to your class.

---

## @Endpoint

**Target:** method in an `@EndpointGen` class

| Parameter | Type | Description |
|-----------|------|-------------|
| `method` | `HTTPMethod` | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` |
| `path` | `String` | Relative path; must start with `/` |

Rules:

- Each `{param}` in `path` must match a Java parameter name (use `@PathVariable("param")` if `-parameters` is off).
- Existing Spring web annotations on parameters are preserved; path tokens get `@PathVariable` even without a source annotation.
- Non-void → `ResponseEntity.ok(body)`; void → **204**.

---

## @DTOField

**Target:** entity field. **Repeatable.**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `dto` | `String` | — | `Read`, `Create`, or `Update` |
| `fieldName` | `String` | `""` | DTO property name; default = entity field name |
| `relation` | `boolean` | `false` | `true`: scalar → `{field}Id` in Create/Update/Read; collection → `{field}Ids` as `List<Long>` (or custom `fieldName`). Resolved via `{Entity}RelationApplier` + `findById` before save |
| `nestedRead` | `boolean` | `false` | Read DTO only: flatten related entity Read `@DTOField` scalars with prefix (e.g. `productCategoryCode`) |

Bean Validation annotations on the entity field are copied to the generated DTO field. Generated controllers use `@Valid` on create/batch bodies; PATCH validates the merged Update DTO.

Related entity types must be `@CrudGen` (repository injected into `{Entity}RelationApplier`). Missing FK → **400**; missing related `@CrudGen` → compile error.

---

## @FindBy

**Target:** field. No attributes.

Generates:

- Repository: `Entity findBy{Field}(Type field)`
- Service: same
- Controller: `GET {controllerPath}/findBy{Field}?{field}=…` → **404** when null

---

## @FindAllBy

**Target:** field. No attributes.

Generates:

- Repository: `List<Entity> findAllBy{Field}(…)` and `Page<Entity> findAllBy{Field}(…, Pageable)`
- Service: list + `{Field}Paged` wrapper for page
- Controller: list and `/paged` query endpoints

---

## Shared generated types

### CrudGenSecurityService

Package: `com.bariskokulu.crudgen.security`

Emitted when **any** `@CrudGen` or `@EndpointGen` has `securityService = true`.

- `checkEntityAccess(entityFqn, method, params…)` — entity canonical name + controller method name
- `checkUseAccess(useCaseKey, params…)` — `com.example.Ops#methodName`

### EntityLifecycleCallbacks&lt;T&gt;

Package: `com.bariskokulu.crudgen.lifecycle`

Emitted when **any** entity has `lifecycleHooks = true`.

Abstract: `beforeCreate`, `afterCreate`, `beforeUpdate`, `afterUpdate`, `beforeDelete`, `afterDelete`.

Default no-ops: `beforeCreateBatch`, `afterCreateBatch`, `beforeUpdateBatch(List<T>)`, `afterUpdateBatch`, `beforeDeleteBatch`, `afterDeleteBatch`.

Batch PATCH: `beforeUpdateBatch` once, then `beforeUpdate` per entity, then `saveAll` (single transaction), then `afterUpdate` per saved entity, then `afterUpdateBatch`.

---

## Mapper (MapStruct)

When Read DTO exists:

- `get(Entity)` → Read DTO
- `create(CreateDTO)` → Entity (if Create exists)
- `toPatch(Entity)` → Update DTO (if Update exists)
- `patch(Entity, UpdateDTO)` — partial update helper

Configure MapStruct + Lombok annotation processor order: **Lombok → MapStruct → CRUDGen**.
