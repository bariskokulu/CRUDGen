# Troubleshooting

## Processor does not run / no generated sources

**Symptoms:** Missing `*Controller`, `*Repository`, compile errors referencing generated types.

**Checks:**

1. CrudGen on `annotationProcessor` (Gradle) or `annotationProcessorPaths` (Maven), not only `compileOnly`.
2. Order: **Lombok → MapStruct → CrudGen**.
3. IDE: enable annotation processing; delegate build to Gradle if generated sources are stale.
4. Clean rebuild: `./gradlew clean compileJava` (or your build tool equivalent).

---

## `"Read" DTO is required`

`controllerPath` is set but `dtos` omits `"Read"` or no field has `@DTOField(dto = "Read")`.

**Fix:** add `"Read"` to `dtos` and at least one `@DTOField(dto = "Read")`.

---

## JSON Patch / Update DTO errors

**Symptoms:** Processor error about Jackson or zjsonpatch; PATCH endpoints missing.

**Cause:** `"Update"` in `dtos` requires Jackson databind + zjsonpatch on the **compile** classpath.

| Stack | zjsonpatch |
|-------|------------|
| Boot 3 / Jackson 2 | `com.flipkart.zjsonpatch:zjsonpatch:0.4.16` |
| Boot 4 / Jackson 3 | `io.github.vishwakarma:zjsonpatch:0.6.2` |

If you do not need PATCH, remove `"Update"` from `dtos`.

---

## MapStruct “Unmapped target property” warnings

Common with `@DTOField(fieldName = "…")` or when `id` is not in DTOs. Warnings do not fail the build.

To silence: add MapStruct `@Mapping` in a decorator or widen DTO field coverage.

---

## `NoSuchBeanDefinitionException: CrudGenSecurityService`

Generated controllers call security when `securityService = true` (default).

**Fix:**

- Register `@Service` implementing `CrudGenSecurityService`, or
- Set `securityService = false` on **every** `@CrudGen` and `@EndpointGen` type (interface then not generated).

---

## `NoSuchBeanDefinitionException: EntityLifecycleCallbacks`

Same pattern as security — implement `EntityLifecycleCallbacks<T>` or set `lifecycleHooks = false` on all entities.

---

## MongoDB in tests without a server

**Symptoms:** `@SpringBootTest` fails for `@CrudGen(repo = MONGO)` when MongoDB is not running.

**Fix:** exclude Mongo autoconfig and disable repository auto-config, or provide a test double of your generated repository.

Boot 4 example property:

```properties
spring.autoconfigure.exclude=\
  org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration,\
  org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration,\
  org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration
spring.data.mongodb.repositories.enabled=false
```

Boot 3 uses `org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration` and `MongoRepositoriesAutoConfiguration` (different package names).

For HTTP tests, supply a `@Primary` mock/stub of the generated `MongoRepository` interface.

---

## Path variable: name not specified

Spring cannot infer parameter names without debug symbols.

**Fix:** `@PathVariable("id")` or compile with `-parameters`.

---

## `@Endpoint` path validation errors

- Path must start with `/`.
- Each `{token}` must match a method parameter **name**.

---

## `findBy*` returns 404

Generated `findBy{Field}` returns **404** when the repository returns null. Multiple DB rows for a `findBy` query are a persistence-layer exception, not a 409 from CrudGen.

---

## Batch endpoint 400

- Batch create/update/delete: max **500** elements.
- Pagination: `page` ≥ 0, `size` between 1 and 500.

---

## PATCH 400 after relation ids

Missing or invalid FK in Create/Update/PATCH → **400** from relation applier. Related entity must be `@CrudGen` with a resolvable `findById`.

---

## `customController` + `relation=true`

Compile error unless you invoke `{Entity}RelationApplier` in your controller/service.

---

## `extendRepo` compile error

`extendRepo` must be a supertype of the contract for `repo` (e.g. `JpaRepository<Entity, Long>` for `JPA`). This does **not** apply to `customRepo` — only when the processor generates a repository.

## `customRepo` compile or runtime errors

Generated service calls PLAIN-style methods on your type (`findById`, `save`, …). Missing methods fail at compile time; wrong signatures fail at runtime. Your type need not extend Spring Data repository interfaces.

---

## H2: GET by id fails after DELETE ALL

H2 `IDENTITY` may not reset on `deleteAll()`. Next insert can get id 2 while tests expect id 1.

**Fix:** reset sequence in test setup, or use ids from create responses instead of hardcoding `1`.

---

## Boot 3 vs Boot 4 dependency matrix

| Area | Boot 3 | Boot 4 |
|------|--------|--------|
| Web | `spring-boot-starter-web` | `spring-boot-starter-webmvc` |
| MapStruct | 1.5.5.Final | 1.6.3 |
| JSON Patch | zjsonpatch 0.4.x | zjsonpatch 0.6.2 |
| Mongo autoconfig packages | `…autoconfigure.mongo…` | `…boot.mongodb…` / `…boot.data.mongodb…` |

---

## Still stuck

1. Rebuild with `--info` and read annotation processor messages.
2. Inspect `build/generated/sources/annotationProcessor/java/main`.
3. Compare your entity against [EXAMPLES.md](EXAMPLES.md) minimal and full profiles.
4. Report: entity source, dependencies, first processor/compile error.
