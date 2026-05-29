# CRUDGen

CRUDGen is a Java annotation processor that automatically generates CRUD (Create, Read, Update, Delete) layers for Spring Boot applications. It generates Repository interfaces, Service classes, REST Controllers, DTOs, and MapStruct mapper interfaces based on simple annotations on your entity classes.

## Features

- **Automatic CRUD Generation**: Generate Repository, Service, Controller, DTOs, and Mapper interfaces with a single annotation
- **Flexible Generation**: Choose what to generate - repository only, repository + service, or full stack
- **Multiple Repository Types**: Spring Data **JPA** (with `JpaSpecificationExecutor`), **MongoDB**, or **PLAIN** (explicit repository interface for JDBC, jOOQ, MyBatis, R2DBC, etc.)
- **Use Case Controllers**: Generate REST controllers for use case services
- **Field-Based Queries**: Automatically generate `findBy` and `findAllBy` methods based on field annotations
- **JSON Patch Support**: Built-in support for JSON Patch (RFC 6902) updates
- **Batch Operations**: Automatic generation of batch create, update, and delete endpoints
- **Pagination**: Built-in pagination support for list endpoints
- **Security Integration**: Optional security service integration for access control
- **Logging**: Optional debug logging in generated code
- **Customization**: Support for custom classes and interface extension
- **Incremental Processing**: Supports incremental annotation processing for faster builds

## Requirements

- **Consumer project:** Java **8+** on the compiler classpath for the processor JAR and **JavaPoet** (the published library targets Java 8 bytecode). Your **app** can use any supported Java release you configure in your build.
- **Building this repo:** Gradle **9.x** with the **Foojay toolchain resolver** (see `settings.gradle.kts`): **`lib`** uses JDK **26** for compilation; example modules use JDK **21**. The `lib` artifact is still built with **`--release 8`**; examples use **`--release 17`**.
- **Spring:** Spring Boot **3** (Spring Framework 6, **Jackson 2**) or Spring Boot **4** (Spring Framework 7, **Jackson 3** / `tools.jackson.*`). Spring Web; **Spring Data JPA or Mongo** *or* **`repo = RepoType.PLAIN`** with your own `@Repository` implementation; **spring-data-commons** **`Page` / `Pageable`** for pagination. **Bean Validation:** the processor picks **`javax.*` vs `jakarta.*`** from the **compile classpath** (only `javax.validation.Validator` → `javax`; otherwise **`jakarta`**). **MapStruct**. **JSON Patch is optional:** omit **`Update`** from `@CrudGen(dtos = …)` and the processor does **not** generate PATCH endpoints, **`ObjectMapper` constructor injection**, or **zjsonpatch** usage. If **`Update`** is present, the processor uses **`javax.lang.model`** (`Elements#getTypeElement`) on the **same javac compile classpath** to require Jackson databind + the matching **zjsonpatch** class (`JsonPatch` vs `Jackson3JsonPatch`). **Jackson 2 vs 3** follows the classpath: **`tools.jackson.databind.ObjectMapper`** present → Jackson 3; otherwise Jackson 2. **`com.flipkart.zjsonpatch:zjsonpatch:0.4.x`** (Jackson 2) or **`io.github.vishwakarma:zjsonpatch:0.6.2+`** (Jackson 3).

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.bariskokulu</groupId>
    <artifactId>crudgen</artifactId>
    <version>1.0.2</version>
    <scope>provided</scope>
</dependency>
```

Use `annotationProcessorPaths` (or your IDE equivalent) so the processor runs at compile time. Add **MapStruct** and **Lombok** (if used) to the same processor path; order should be **Lombok → MapStruct → CRUDGen** where possible.

### Gradle

```kotlin
dependencies {
    compileOnly("io.github.bariskokulu:crudgen:1.0.2")
    annotationProcessor("org.projectlombok:lombok:1.18.42") // if you use Lombok
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    annotationProcessor("io.github.bariskokulu:crudgen:1.0.2")
}
```

## Quick Start

### Basic Entity with Full CRUD

```java
@CrudGen(
    controllerPath = "/api/users",
    dtos = {"Read", "Create", "Update"}
)
@Entity
public class User {
    @Id
    private Long id;
    
    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    @DTOField(dto = "Update")
    private String name;
    
    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    private String email;
}
```

This single annotation generates:
- `UserRepository` - JPA repository interface extending `JpaRepository<User, Long>` and `JpaSpecificationExecutor<User>`
- `UserService` - Service class with CRUD methods
- `UserController` - REST controller with full CRUD endpoints
- `UserReadDTO`, `UserCreateDTO`, `UserUpdateDTO` - Data transfer objects
- `UserMapper` - MapStruct mapper interface

### Repository Only

```java
@CrudGen
@Entity
public class Product {
    @Id
    private Long id;
    private String name;
}
```

Generates only `ProductRepository`.

### Repository only, database-agnostic (`PLAIN`)

```java
import com.bariskokulu.crudgen.util.RepoType;

@CrudGen(repo = RepoType.PLAIN)
@Entity
public class LedgerEntry {
    @Id
    private Long id;
}
```

Generates a `LedgerEntryRepository` interface annotated with `@Repository` and **explicit** methods (`findById` → `Optional`, `findAll`, `findAll(Pageable)`, `save`, …) with **no** `JpaRepository` / `MongoRepository` supertype. Implement that interface with **JdbcTemplate**, **jOOQ**, **MyBatis**, **R2DBC**, etc. Pagination in generated code still uses Spring’s **`Page`** / **`Pageable`** (from `spring-data-commons`).

### Service + Repository (No Controller)

```java
@CrudGen(service = true)
@Entity
public class Order {
    @Id
    private Long id;
}
```

Generates `OrderRepository` and `OrderService`.

## Generated Endpoints

When a controller is generated (by setting `controllerPath`), the following REST endpoints are automatically created:

### Standard CRUD Endpoints

- `GET /{controllerPath}/{id}` - Get entity by ID
- `GET /{controllerPath}/` - Get all entities
- `GET /{controllerPath}/paged?page=0&size=20` - Get paginated entities
- `POST /{controllerPath}` - Create entity
- `POST /{controllerPath}/batch` - Create multiple entities
- `PATCH /{controllerPath}/{id}` - Update entity (JSON Patch)
- `PATCH /{controllerPath}/batch` - Update multiple entities (JSON Patch)
- `DELETE /{controllerPath}/{id}` - Delete entity by ID
- `DELETE /{controllerPath}/batch` - Delete multiple entities

### Field-Based Query Endpoints

If you annotate fields with `@FindBy` or `@FindAllBy`, additional endpoints are generated:

- `GET /{controllerPath}/findBy{FieldName}?{fieldName}=value` - Find single entity by field
- `GET /{controllerPath}/findAllBy{FieldName}?{fieldName}=value` - Find all entities by field
- `GET /{controllerPath}/findAllBy{FieldName}/paged?{fieldName}=value&page=0&size=20` - Find paginated entities by field

## Use Case Controllers

Generate REST controllers for use case services:

```java
@EndpointGen(controllerPath = "/api/payments")
public class PaymentService {
    
    @Endpoint(method = HTTPMethod.POST, path = "/process")
    public PaymentResult processPayment(PaymentRequest request) {
        // your implementation
    }
    
    @Endpoint(method = HTTPMethod.GET, path = "/history/{userId}")
    public List<Payment> getHistory(@PathVariable Long userId) {
        // your implementation
    }
}
```

This generates a `PaymentServiceController` with REST endpoints that delegate to your service methods.

## Documentation

- [Decision tree: what gets generated](docs/DECISION-TREE.md)
- [Complete Annotation Reference](docs/ANNOTATIONS.md) - Detailed documentation of all annotations and their parameters
- [Examples](docs/EXAMPLES.md) - Comprehensive usage examples
- [Troubleshooting](docs/TROUBLESHOOTING.md) - Common issues and solutions

## Verifying the processor (this repo)

Four compile-only modules: **simple-boot3** / **simple-boot4** (minimal entity, Read-only, flags off) and **complex-boot3** / **complex-boot4** (full processor surface: JPA + Mongo + PLAIN, custom repo/service/controller, `service` without REST, generated-package overrides, extend mixins, `DTOField.fieldName`, JSON Patch, all `HTTPMethod`s on `@Endpoint`, optional `@RequestParam`). Boot 4 twins share sources via `sourceSets`. Jackson and Bean Validation packages are inferred from the compile classpath.

```bash
./gradlew compileAllExamples
```

On Windows use `gradlew.bat compileAllExamples` from the repo root.

Per module: [simple-boot3/README.md](simple-boot3/README.md), [simple-boot4/README.md](simple-boot4/README.md), [complex-boot3/README.md](complex-boot3/README.md), [complex-boot4/README.md](complex-boot4/README.md).

## How It Works

CRUDGen is a compile-time annotation processor. During compilation:

1. The processor scans for classes annotated with `@CrudGen` or `@EndpointGen`
2. It analyzes the entity structure, fields, and annotations
3. It generates the appropriate Java source files
4. The generated files are compiled along with your code

All generated code is placed in the standard generated sources directory and can be viewed in your IDE.

## Security Integration

If `securityService = true` (default) on **any** `@CrudGen` or `@EndpointGen` type, the processor emits the `CrudGenSecurityService` interface in `com.bariskokulu.crudgen.security` and generated controllers call it. Implement it in your app:

```java
@Service
public class MySecurityService implements CrudGenSecurityService {
    @Override
    public void checkEntityAccess(String entityClassName, String method, Object... params) {
        // Check access for entity operations
    }
    
    @Override
    public void checkUseAccess(String method, Object... params) {
        // Check access for use case operations
    }
}
```

If **every** annotated type sets `securityService = false`, that interface stub is **not** generated.

## Lifecycle hooks

If `lifecycleHooks = true` (default) on **any** `@CrudGen` entity, the processor emits `com.bariskokulu.crudgen.lifecycle.EntityLifecycleCallbacks<T>` and injects it into the generated service/controller (`@Nullable`; no-op if omitted). You implement **beforeCreate**, **afterCreate**, **beforeUpdate**, **afterUpdate**, **beforeDelete**, **afterDelete**; batch hooks (**\*Batch**) are `default` no-ops unless you override them. If **all** entities use `lifecycleHooks = false`, that interface is **not** generated.

## License

Apache License 2.0 - see [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

