---
name: crudgen
description: Use CrudGen (io.github.bariskokulu:crudgen)—annotate entities and services, configure the processor, implement security/lifecycle/PLAIN repos; know which layers compile will emit.
---

# CrudGen

You annotate classes and add the processor; compile emits Spring components. Artifact `io.github.bariskokulu:crudgen:1.0.2`, Boot 3 or 4. Generated Java lands in `build/generated/sources/annotationProcessor/java/main/` or `target/generated-sources/annotations/`.

## What you add to the build

| You need | When |
|----------|------|
| `crudgen` on the annotation-processor path | always |
| `mapstruct` + `mapstruct-processor`, MapStruct `componentModel = "spring"` | any entity with non-empty `controllerPath` |
| `spring-boot-starter-web`, `spring-boot-starter-validation`, data-jpa or data-mongodb | typical app; skip data starters if `repo = PLAIN` only |
| `spring-boot-starter-json` + zjsonpatch `0.4.x` (Boot 3) or `0.6.2+` (Boot 4) | you put `"Update"` in `dtos` |

```kotlin
dependencies {
    compileOnly("io.github.bariskokulu:crudgen:1.0.2")
    annotationProcessor("io.github.bariskokulu:crudgen:1.0.2")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
}
```
Drop MapStruct if no entity uses `controllerPath`.

## Entity: `@CrudGen` on your class

Put it on the persistence model (`@Entity` / `@Document`). Mark the id field with Spring Data `@Id` or JPA `@Id`.

**Choose layers** (what compile will emit besides your entity):

| Your settings | You get (in `packageName`, default = entity package) |
|---------------|------------------------------------------------------|
| defaults | `{Entity}Repository` |
| `service = true` | + `{Entity}Service` |
| `controllerPath = "/api/…"` (must start with `/`) | + `{Entity}Service`, `{Entity}ReadDTO` / `CreateDTO` / `UpdateDTO` per `dtos`, `{Entity}Mapper`, `{Entity}Controller` |
| `customRepo` / `customService` / `customController` | that piece not generated—you supply the class |
| `extendRepo` / `extendService` / `extendController` | generated type implements your interface |

`controllerPath` set ⇒ `"Read"` must be in `dtos` or compile fails. `dtos` without `controllerPath` produces no DTO/mapper/controller files.

**`dtos` (your API contract choices)**

| Value | You do | Compile emits |
|-------|--------|---------------|
| `Read` | `@DTOField(dto = "Read")` on fields to expose | `{Entity}ReadDTO` + mapper `get` |
| `Create` | `@DTOField(dto = "Create")` on writable create fields | `{Entity}CreateDTO` + create flow in generated stack |
| `Update` | `@DTOField(dto = "Update")` on patchable fields; add json + zjsonpatch deps | `{Entity}UpdateDTO` + JSON Patch update flow in generated stack |

PATCH clients send **RFC 6902** JSON Patch (`Content-Type: application/json-patch+json`) against the **Update DTO JSON** shape, not a partial entity document.

`@DTOField(dto, fieldName?)` is repeatable—only marked fields appear on DTOs. JPA/Mongo associations (`@OneToMany`, etc.) are not modeled; expose scalars you annotate or handle relations yourself.

**`repo` (your persistence backend)** — only when `extendRepo` is unset:

| Value | You do |
|-------|--------|
| `JPA` (default) | Provide Spring Data JPA; generated repo extends `JpaRepository` |
| `MONGO` | Provide Spring Data Mongo; generated repo extends `MongoRepository` |
| `PLAIN` | Implement generated `{Entity}Repository` (JDBC, jOOQ, etc.) |

**`extendRepo`:** generated repo extends your interface only (+ `@FindBy` / `@FindAllBy` method signatures). You must declare CRUD/query methods the generated service will call, or inherit them.

**`customRepo` / `customService`:** you register beans matching `{repositoryName}` / `{serviceName}` (defaults `{Entity}Repository`, `{Entity}Service`) in `packageName`, or you replace the other layer too and wire yourself.

**Field markers (optional)**

- `@FindBy` on a field — add single-entity lookup by that field to generated repo/service/API.
- `@FindAllBy` on a field — add list + paged lookup by that field.

**Flags you toggle on the annotation**

| Attribute | Default | Your intent when `false` |
|-----------|---------|---------------------------|
| `securityService` | `true` | Generated code won't call security; stub interface omitted if all types disable it |
| `lifecycleHooks` | `true` | No lifecycle callbacks; stub omitted if all entities disable it |
| `logging` | `true` | No debug logs in generated classes |
| `openApi` | `true` | No OpenAPI annotations on generated controllers (still need springdoc in app if `true`) |
| `repositoryName`, `serviceName`, `controllerName`, `packageName` | `{Entity}*` / entity package | Rename or relocate generated types |

## Use case: `@EndpointGen` + `@Endpoint`

On **your** service/application class:

1. `@EndpointGen(controllerPath = "/api/…")` on the class.
2. `@Endpoint(method = HTTPMethod.*, path = "…")` on each method you want as HTTP.
3. Keep Spring binding annotations on parameters (`@PathVariable`, `@RequestBody`, `@RequestParam`).

Compile emits `{Class}Controller` in `packageName` (default: same package as your class). Only `@Endpoint` methods are wired. Your class stays the implementation; the generated class is the HTTP adapter.

| `EndpointGen` attribute | Default |
|-------------------------|---------|
| `controllerName`, `packageName` | `{Class}Controller`, class package |
| `securityService`, `logging`, `openApi` | `true` (same meaning as on `@CrudGen`) |

No `lifecycleHooks` on `@EndpointGen`.

## What you implement (not generated logic)

**`CrudGenSecurityService`** — one `@Component` if any `@CrudGen` / `@EndpointGen` keeps `securityService = true` (default). Interface is generated once per compile:

```java
void checkEntityAccess(String entityClassName, String method, Object... params);
void checkUseAccess(String method, Object... params);
```

**`EntityLifecycleCallbacks<YourEntity>`** — one bean per entity type if that entity keeps `lifecycleHooks = true` (default). Interface generated once; implement the before/after hooks you need (batch hooks optional).

**`PLAIN` repository** — concrete `@Repository` implementing the generated interface.

Generated `{Entity}Service` applies `@Transactional` on save/delete mutators—you do not configure that via CrudGen.

To drop stubs: set `securityService` / `lifecycleHooks` to `false` on every annotated type, then clean compile.

## Annotate (minimal)

```java
@CrudGen(controllerPath = "/api/items", dtos = {"Read", "Create", "Update"})
@Entity class Item {
  @Id Long id;
  @DTOField(dto = "Read") @DTOField(dto = "Create") @DTOField(dto = "Update") String sku;
}

@CrudGen(repo = RepoType.PLAIN, service = true)
@Entity class Row { @Id Long id; }

@EndpointGen(controllerPath = "/api/jobs")
class JobApi {
  @Endpoint(method = HTTPMethod.POST, path = "/run") void run(@RequestBody RunCmd cmd) { }
}
```

After compile, open generated sources for exact REST paths and method bodies—do not duplicate that surface in app code.
