# Examples

Progressive recipes for **`io.github.bariskokulu:crudgen`**.  
**Minimal profile** = smallest working REST API. **Full profile** = every feature the processor can emit.

Install and processor order: see root `README.md` or agent skill `crudgen`. Parameter tables: [ANNOTATIONS.md](ANNOTATIONS.md). Generation rules: [DECISION-TREE.md](DECISION-TREE.md).

---

## 1. Minimal profile — one entity, HTTP CRUD

Use when you want JSON list/get/create/delete with the least configuration.

```java
@Entity
@Getter @Setter
@CrudGen(
    controllerPath = "/api/widgets",
    dtos = { "Read", "Create" },
    securityService = false,
    lifecycleHooks = false,
    openApi = false,
    logging = false
)
public class Widget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    @NotBlank
    @Size(max = 200)
    private String name;
}
```

**Gradle:** `compileOnly` + `annotationProcessor` for CrudGen; MapStruct processor if you use Lombok on entities. Default `repo = JPA` → Spring Data JPA repository.

**Emitted:** `{Entity}Repository`, `{Entity}Service`, `{Entity}Controller`, `{Entity}ReadDTO`, `{Entity}CreateDTO`, `{Entity}Mapper`.

**HTTP (base = `controllerPath`):** `GET /`, `GET /paged`, `GET /{id}`, `POST /`, `POST /batch`, `DELETE /{id}`, `DELETE /batch`. No PATCH without `"Update"` in `dtos`.

Turn off `securityService` / `lifecycleHooks` until you register `CrudGenSecurityService` and `EntityLifecycleCallbacks` beans, or implement those interfaces.

---

## 2. Full profile — JSON Patch updates

Add `"Update"` and zjsonpatch on the **compile** classpath:

| Stack | zjsonpatch |
|-------|------------|
| Spring Boot 3 / Jackson 2 | `com.flipkart.zjsonpatch:zjsonpatch:0.4.16` |
| Spring Boot 4 / Jackson 3 | `io.github.vishwakarma:zjsonpatch:0.6.2` |

```java
@CrudGen(
    controllerPath = "/api/products",
    dtos = { "Read", "Create", "Update" }
)
@Entity
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    @DTOField(dto = "Update")
    private String sku;
}
```

```http
PATCH /api/products/1
Content-Type: application/json-patch+json

[{"op":"replace","path":"/sku","value":"NEW-SKU"}]
```

Batch: `PATCH /api/products/batch` with body `{ "1": [ …ops… ], "2": [ …ops… ] }` (max 500 ids). Patch paths use **Update DTO property names** (after `@DTOField(fieldName=…)`).

---

## 3. Field queries (`@FindBy`, `@FindAllBy`)

```java
@FindBy
@DTOField(dto = "Read")
@DTOField(dto = "Create")
private String sku;

@FindAllBy
@DTOField(dto = "Read")
private String category;
```

→ `GET /api/products/findBySku?sku=…` (404 if null)  
→ `GET /api/products/findAllByCategory?category=…`  
→ `GET /api/products/findAllByCategory/paged?category=…&page=0&size=20`

---

## 4. Rename API fields (`fieldName`)

```java
@DTOField(dto = "Read", fieldName = "displayTitle")
@DTOField(dto = "Create", fieldName = "displayTitle")
@DTOField(dto = "Update", fieldName = "displayTitle")
private String internalTitle;
```

JSON and PATCH use `displayTitle`. MapStruct may warn about unmapped entity fields — expected unless you add mapping config.

---

## 5. Relations (`relation`, `nestedRead`)

Related types must also be `@CrudGen`. Scalar FK → `{field}Id` in DTOs; collections → `{field}Ids` as `List<Long>`.

```java
@ManyToOne
@DTOField(dto = "Read", relation = true, nestedRead = true)
@DTOField(dto = "Create", relation = true)
@DTOField(dto = "Update", relation = true)
private Category category;

@OneToMany
@DTOField(dto = "Read", relation = true)
@DTOField(dto = "Create", relation = true)
@DTOField(dto = "Update", relation = true)
private List<Tag> tags;
```

Processor emits `{Entity}RelationApplier`: loads related rows via `findById`, sets managed references before `save`. Missing id → **400**.

`nestedRead = true` on a scalar adds prefixed Read scalars from the related entity’s Read `@DTOField`s (e.g. `categoryCode`).

`relation = false` keeps entity types on the DTO — you own serialization and lazy loading.

**Not supported with `customController`:** use generated controller or call `{Entity}RelationApplier` yourself.

---

## 6. Output package and type names

```java
@CrudGen(
    packageName = "com.example.product.api",
    repositoryName = "ProductStore",
    serviceName = "ProductService",
    controllerName = "ProductController",
    controllerPath = "/api/products",
    dtos = { "Read", "Create", "Update" }
)
```

Marker interfaces in your domain package:

```java
public interface ProductRepoExt extends JpaRepository<Product, Long> {}
```

```java
@CrudGen(
    extendRepo = ProductRepoExt.class,
    extendService = ProductServiceExt.class,
    extendController = ProductControllerExt.class,
    /* … */
)
```

`extendRepo` must subtype `JpaRepository` / `MongoRepository` for the chosen `repo`. Generated repository **only** extends `extendRepo` (no extra generated superinterfaces).

---

## 7. `RepoType` variants

| `repo` | You provide | Generated |
|--------|-------------|-----------|
| `JPA` (default) | Spring Data JPA on classpath | `JpaRepository` + `JpaSpecificationExecutor` |
| `MONGO` | Spring Data Mongo | `MongoRepository` |
| `PLAIN` | `@Repository` implementation of generated interface | Explicit CRUD + query method signatures |

**PLAIN + custom implementation:**

```java
@CrudGen(
    repo = RepoType.PLAIN,
    service = true,
    customRepo = PlainProductRepository.class,
    securityService = false,
    lifecycleHooks = false
)
@Entity
public class PlainProduct {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

Implement `findById`, `findAll`, `findAll(Pageable)`, `save`, `saveAll`, `deleteById`, `deleteAllById`, plus any `@FindBy` / `@FindAllBy` methods on the generated interface.

**Mongo HTTP entity:**

```java
@Document(collection = "tags")
@CrudGen(
    repo = RepoType.MONGO,
    controllerPath = "/api/tags",
    dtos = { "Read", "Create", "Update" }
)
public class Tag {
    @Id
    private String id;
    /* @DTOField … */
}
```

---

## 8. Skip generated layers (`custom*`)

| Goal | Annotation |
|------|------------|
| Your repository only | `customRepo = YourRepo.class` |
| Your service | `customService = YourService.class` |
| Your REST layer | `customController = YourController.class` |

**`customRepo`:** Your bean does **not** need to extend `JpaRepository` or `MongoRepository`. It must implement the methods the generated service calls — same set as `RepoType.PLAIN` (`findById` → `Optional`, `findAll`, `findAll(Pageable)`, `save`, `saveAll`, `deleteById`, `deleteAllById`, plus any `@FindBy` / `@FindAllBy` signatures). Typical pairing: `repo = PLAIN` and `customRepo = YourRepositoryImpl.class`.

Skipped layers are not generated; other layers still follow `controllerPath` / `dtos` rules. Example: `customController` still generates service, repo, DTOs, mapper, relation applier (if relations + no custom controller conflict).

**Service only, no HTTP:**

```java
@CrudGen(service = true)
@Entity
public class BackgroundJob {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

→ repository + service only.

**Repository only:**

```java
@CrudGen
@Entity
public class AuditRow {
    @Id
    private Long id;
}
```

→ repository only (default `service = false`, empty `controllerPath`).

---

## 9. Use-case HTTP (`@EndpointGen`)

Your class stays the Spring bean; processor adds a `@RestController` adapter.

```java
@EndpointGen(
    controllerPath = "/api/ops",
    securityService = true,
    openApi = true,
    logging = true
)
@Component
public class Operations {

    @Endpoint(method = HTTPMethod.GET, path = "/ping")
    public String ping() { return "pong"; }

    @Endpoint(method = HTTPMethod.PUT, path = "/items/{id}")
    public String putItem(@PathVariable("id") Long id, @RequestBody String body) {
        return id + ":" + body;
    }

    @Endpoint(method = HTTPMethod.DELETE, path = "/items/{id}")
    public void deleteItem(@PathVariable("id") Long id) { }
}
```

Void methods → **204**. Other returns → **200** in `ResponseEntity`. Security: `checkUseAccess("com.example.Operations#ping", …)`. No entity lifecycle hooks on use-case controllers.

---

## 10. Security and lifecycle (defaults on)

When any `@CrudGen` or `@EndpointGen` keeps `securityService = true`, implement:

```java
@Service
public class AppSecurity implements CrudGenSecurityService {
    @Override
    public void checkEntityAccess(String entityClassName, String method, Object... params) { }

    @Override
    public void checkUseAccess(String useCaseKey, Object... params) { }
}
```

When any entity keeps `lifecycleHooks = true`, implement `EntityLifecycleCallbacks<T>` (`beforeCreate`, `afterCreate`, `beforeUpdate`, `afterUpdate`, `beforeDelete`, `afterDelete`, plus optional batch defaults).

Hooks run on the **generated entity controller** only. Batch PATCH order: `beforeUpdateBatch` → per-entity `beforeUpdate` → patch + save → per-entity `afterUpdate` → `afterUpdateBatch` (single `saveAll` transaction).

Disable per type: `securityService = false`, `lifecycleHooks = false`. If **all** annotated types disable a flag, that shared interface is not generated.

---

## 11. Full profile checklist

Use this when you need **maximal** library coverage in one application:

| Feature | Enable via |
|---------|------------|
| JPA + extend repo/service/controller | `@CrudGen` defaults + `extend*` |
| All three DTOs + PATCH + batch | `dtos = {Read, Create, Update}` + zjsonpatch |
| `@FindBy` / `@FindAllBy` | On entity fields |
| `fieldName`, `relation`, `nestedRead` | `@DTOField` |
| `packageName`, `*Name` | `@CrudGen` |
| `RepoType.MONGO` | `repo = MONGO` |
| `RepoType.PLAIN` + impl | `repo = PLAIN`, `customRepo` |
| `customService` | Hand-written service |
| `customController` | Hand-written REST (no `relation=true` unless you wire applier) |
| `service = true`, no HTTP | Headless service |
| `@EndpointGen` all HTTP verbs | `@Endpoint` per method |
| Security + lifecycle + OpenAPI + logging | Defaults `true` + app beans |

---

## 12. After compile

Inspect `build/generated/sources/annotationProcessor/java/main` — do not re-implement generated endpoints manually.

Failures: [TROUBLESHOOTING.md](TROUBLESHOOTING.md).
