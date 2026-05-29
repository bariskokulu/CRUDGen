# Examples

Working code lives in [`samples/`](../samples/). This page maps common goals to those modules and shows the core patterns inline.

---

## 1. Minimal REST API

**Module:** [samples/simple-boot3](../samples/simple-boot3) (Boot 3) or [samples/simple-boot4](../samples/simple-boot4) (Boot 4)

Smallest setup that still returns JSON from a real controller:

```java
@CrudGen(
    controllerPath = "/api/widgets",
    dtos = { "Read", "Create" },
    securityService = false,
    lifecycleHooks = false,
    openApi = false,
    logging = false
)
@Entity
public class Widget {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    @NotBlank
    private String name;
}
```

Turn off security/lifecycle in the annotation when you have not registered `CrudGenSecurityService` / `EntityLifecycleCallbacks` beans yet.

---

## 2. Full CRUD with JSON Patch

**Module:** `MegaProduct` in [samples/complex-boot3](../samples/complex-boot3)

```java
@CrudGen(
    controllerPath = "/api/mega-products",
    dtos = { "Read", "Create", "Update" },
    packageName = "com.example.product.api",
    repositoryName = "MegaProductStore"
)
@Entity
public class MegaProduct { /* @FindBy / @FindAllBy / @DTOField */ }
```

Add to Gradle when using `Update`:

```kotlin
compileOnly("io.github.vishwakarma:zjsonpatch:0.6.2") // Boot 4 / Jackson 3
// Boot 3 / Jackson 2: com.flipkart.zjsonpatch:zjsonpatch:0.4.16
```

Patch request:

```http
PATCH /api/mega-products/1
Content-Type: application/json-patch+json

[{"op":"replace","path":"/sku","value":"NEW-SKU"}]
```

Batch patch body: JSON object `{ "1": […patch…], "2": […patch…] }`.

---

## 3. Field queries

**Module:** `MegaProduct`, `MongoTag` in complex samples

```java
@FindBy
@DTOField(dto = "Read")
private String sku;

@FindAllBy
@DTOField(dto = "Read")
private String category;
```

→ `GET /api/mega-products/findBySku?sku=…`  
→ `GET /api/mega-products/findAllByCategory?category=…`  
→ `GET /api/mega-products/findAllByCategory/paged?category=…&page=0&size=20`

---

## 4. Custom names and packages

**Module:** `MegaProduct` in complex samples

```java
@CrudGen(
    packageName = "com.example.crudgen.complex.gen",
    controllerName = "MegaProductController",
    serviceName = "MegaProductService",
    repositoryName = "MegaProductStore",
    extendRepo = MegaProductRepoExt.class,
    extendService = MegaProductServiceExt.class,
    extendController = MegaProductControllerExt.class,
    /* … */
)
```

Generated types land in `packageName`; your marker interfaces stay in the entity package.

---

## 5. Custom service / controller / repository

| Pattern | Module entity | Annotation |
|---------|---------------|------------|
| Hand-written service | `ManualShelf` | `customService = ManualShelfService.class` |
| Hand-written controller | `BespokeItem` | `customController = BespokeItemController.class` |
| PLAIN repo impl | `PlainCustomer` | `repo = PLAIN`, `customRepo = PlainCustomerRepositoryImpl.class` |

Generated layers not replaced still compile (e.g. `customController` → service + repo + DTOs still generated).

---

## 6. MongoDB entity

**Module:** `MongoTag` in complex samples

```java
@Document(collection = "mongo_tags")
@CrudGen(
    repo = RepoType.MONGO,
    controllerPath = "/api/mongo-tags",
    dtos = { "Read", "Create", "Update" }
)
public class MongoTag {
    @Id
    private String id;
    /* … */
}
```

Tests without MongoDB: exclude Mongo autoconfig and provide a test `@Primary` mock repository — see [complex-boot4/README.md](../samples/complex-boot4/README.md).

---

## 7. Service only (no HTTP)

**Module:** `HeadlessTask` in complex samples

```java
@CrudGen(service = true)
@Entity
public class HeadlessTask {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

Generates repository + service; no controller or DTOs.

---

## 8. Use-case REST (`@EndpointGen`)

**Module:** `FullHttpOps`, `EdgeOps` in complex samples

```java
@EndpointGen(
    controllerPath = "/api/ops",
    securityService = true,
    openApi = true
)
@Component
public class FullHttpOps {

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

Void methods return **204**. Non-void → **200** with body.

---

## 9. DTO field rename

**Module:** `MegaProduct.internalTitle` → DTO `displayTitle`

```java
@DTOField(dto = "Read", fieldName = "displayTitle")
@DTOField(dto = "Create", fieldName = "displayTitle")
@DTOField(dto = "Update", fieldName = "displayTitle")
private String internalTitle;
```

MapStruct may warn about unmapped properties — expected unless you add mapping config.

---

## 10. Security and lifecycle

Implement once in your application:

```java
@Service
public class AppSecurity implements CrudGenSecurityService {
    public void checkEntityAccess(String entityClassName, String method, Object... params) { }
    public void checkUseAccess(String method, Object... params) { }
}

@Component
public class AppLifecycle implements EntityLifecycleCallbacks<MyEntity> {
    public void beforeCreate(MyEntity e) { }
    public void afterCreate(MyEntity e) { }
    /* … remaining hooks … */
}
```

Or disable per entity: `securityService = false`, `lifecycleHooks = false`.

Test stubs: `AllowAllSecurityService`, `NoopLifecycleCallbacks` in complex sample `support` packages.

---

## Verify examples in this repo

```bash
./gradlew verifyAllExamples
```

Decision logic: [DECISION-TREE.md](DECISION-TREE.md).  
Failures: [TROUBLESHOOTING.md](TROUBLESHOOTING.md).
