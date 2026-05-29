# Troubleshooting

## Processor does not run / no generated sources

**Symptoms:** Missing `*Controller`, `*Repository`, compile errors referencing generated types.

**Checks:**

1. CRUDGen on `annotationProcessor` (Gradle) or `annotationProcessorPaths` (Maven), not only `compileOnly`.
2. Processor order: **Lombok → MapStruct → CRUDGen**.
3. IDE: enable annotation processing; delegate build to Gradle if generated sources are stale.
4. Clean rebuild: `./gradlew clean compileJava`.

---

## `"Read" DTO is required`

`controllerPath` is set but `dtos` omits `"Read"` or no field has `@DTOField(dto = "Read")`.

Fix: add `"Read"` to `dtos` and mark at least one field with `@DTOField(dto = "Read")`.

---

## JSON Patch / Update DTO errors

**Symptoms:** Processor error about Jackson or zjsonpatch; PATCH endpoints missing.

**Cause:** `"Update"` in `dtos` requires Jackson databind + zjsonpatch on the **compile** classpath.

| Stack | zjsonpatch coordinate |
|-------|------------------------|
| Boot 3 / Jackson 2 | `com.flipkart.zjsonpatch:zjsonpatch:0.4.16` |
| Boot 4 / Jackson 3 | `io.github.vishwakarma:zjsonpatch:0.6.2` |

If you do not need PATCH, remove `"Update"` from `dtos`.

---

## MapStruct “Unmapped target property” warnings

Common with `@DTOField(fieldName = "…")` or when id is not in DTOs. Complex samples intentionally trigger these; warnings do not fail the build.

To silence: add MapStruct `@Mapping` in a custom mapper decorator or adjust DTO field coverage.

---

## `NoSuchBeanDefinitionException: CrudGenSecurityService`

Generated controller expects security when `securityService = true` (default).

Fix one of:

- Register a `@Service` implementing `CrudGenSecurityService`.
- Set `securityService = false` on every `@CrudGen` / `@EndpointGen` type (then the interface is not generated).

Sample: `AllowAllSecurityService` in complex modules’ test `support` package.

---

## `NoSuchBeanDefinitionException: EntityLifecycleCallbacks`

Same pattern as security — implement `EntityLifecycleCallbacks<T>` or set `lifecycleHooks = false` on all entities.

Sample: `NoopLifecycleCallbacks`.

---

## MongoDB in tests: `NoSuchBeanDefinitionException: mongoTemplate`

**Symptoms:** `@SpringBootTest` fails when a `@CrudGen(repo = MONGO)` entity is on the classpath but no MongoDB runs.

**Fix (Boot 4):** exclude autoconfig:

```properties
spring.autoconfigure.exclude=\
  org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration,\
  org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration,\
  org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration
spring.data.mongodb.repositories.enabled=false
```

Or in `@SpringBootApplication(exclude = { … })` with the same classes.

**Boot 3** uses `org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration` and `MongoRepositoriesAutoConfiguration` (package differs from Boot 4).

Provide a `@Primary` `@Bean` mock of your generated `MongoTagRepository` for HTTP tests, and exclude the entity class from JPA scan if it is not a JPA entity. See `ComplexBoot4TestApplication` and `MongoTagTestSupport` in [complex-boot4](../samples/complex-boot4).

---

## Path variable: `Name for argument … not specified`

Spring cannot infer parameter names without debug symbols.

Fix: explicit names:

```java
@GetMapping("/by-key/{externalKey}")
public ResponseEntity<?> byKey(@PathVariable("externalKey") String externalKey)
```

Or compile with `-parameters`.

---

## `@Endpoint` path validation errors

- Path must start with `/`.
- Each `{token}` in the path must match a method parameter **name** exactly.

---

## `findBy*` returns 404

By design: generated `findBy{Field}` controller methods return **404** when the repository returns null.

---

## Batch endpoint 400

- Batch create/update/delete: max **500** elements.
- Pagination: `page` ≥ 0, `size` between 1 and 500.

---

## H2 tests: GET by id fails after POST

H2 `IDENTITY` does not reset on `deleteAll()`. Next insert may get id 2 while tests expect id 1.

Fix: reset sequence in `@BeforeEach`:

```sql
ALTER TABLE simple_widget ALTER COLUMN id RESTART WITH 1
```

Or parse the id from the create response / query list instead of hardcoding `1`. Simple samples use the `ALTER TABLE` approach.

---

## Boot 3 vs Boot 4 sample differences

| Area | Boot 3 | Boot 4 |
|------|--------|--------|
| Web starter | `spring-boot-starter-web` | `spring-boot-starter-webmvc` |
| Test starter | `spring-boot-starter-test` | `spring-boot-starter-webmvc-test` |
| MapStruct | 1.5.5.Final | 1.6.3 |
| JSON Patch | zjsonpatch 0.4.x | zjsonpatch 0.6.2 |
| Mongo autoconfig packages | `…autoconfigure.mongo…` | `…boot.mongodb…` / `…boot.data.mongodb…` |

Samples are duplicated per Boot version — do not symlink or copy sources between them.

---

## Still stuck

1. Run `./gradlew :your-module:compileJava --info` and read processor messages.
2. Inspect `build/generated/sources/annotationProcessor/java/main`.
3. Compare with [samples/complex-boot3](../samples/complex-boot3) for a working baseline.
4. Open an issue with entity source, `build.gradle`, and the first processor/compile error.
