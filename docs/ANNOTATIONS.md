# Annotation reference

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
| `customRepo` | `Class<?>` | `Void` | Skip generated repository; use this implementation |
| `customController` | `Class<?>` | `Void` | Skip generated controller |
| `customService` | `Class<?>` | `Void` | Skip generated service |
| `extendRepo` | `Class<?>` | `Void` | Generated repository extends this interface |
| `extendService` | `Class<?>` | `Void` | Generated service implements this interface |
| `extendController` | `Class<?>` | `Void` | Generated controller implements this interface |
| `securityService` | `boolean` | `true` | Call `CrudGenSecurityService` from generated controllers |
| `logging` | `boolean` | `true` | SLF4J debug logs in generated code |
| `openApi` | `boolean` | `true` | Swagger v3 annotations on generated controllers |
| `lifecycleHooks` | `boolean` | `true` | Inject `EntityLifecycleCallbacks<T>` into generated controller/service |

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

Bean Validation annotations on the entity field are copied to the generated DTO field. Generated controllers use `@Valid` on create/batch bodies; PATCH validates the merged Update DTO.

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

Default no-ops: `beforeCreateBatch`, `afterCreateBatch`, `beforeUpdateBatch`, `afterUpdateBatch`, `beforeDeleteBatch`, `afterDeleteBatch`.

---

## Mapper (MapStruct)

When Read DTO exists:

- `get(Entity)` → Read DTO
- `create(CreateDTO)` → Entity (if Create exists)
- `toPatch(Entity)` → Update DTO (if Update exists)
- `patch(Entity, UpdateDTO)` — partial update helper

Configure MapStruct + Lombok annotation processor order: **Lombok → MapStruct → CRUDGen**.
