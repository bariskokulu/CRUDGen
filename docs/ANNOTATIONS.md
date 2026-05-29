# Annotation Reference

Complete reference for all CRUDGen annotations and their parameters.

## @CrudGen

The main annotation for generating CRUD layers from entity classes.

### Target
- `ElementType.TYPE` - Applied to class declarations

### Parameters

| Parameter | Type | Default | Required | Description |
|-----------|------|---------|----------|-------------|
| `repo` | `RepoType` | `JPA` | No | Repository type: `JPA`, `MONGO`, or `PLAIN` (explicit CRUD contract, no Spring Data store interface) |
| `service` | `boolean` | `false` | No | Generate service layer. If `false`, service is only generated when `controllerPath` is set |
| `controllerPath` | `String` | `""` | No | REST controller base path (e.g., `/api/users`). If empty, no controller/DTOs/mapper are generated. Must start with `/` if not empty |
| `dtos` | `String[]` | `{}` | No | DTO types to generate. Valid values: `"Read"`, `"Create"`, `"Update"`. `"Read"` is required if `controllerPath` is set |
| `controllerName` | `String` | `""` | No | Custom controller class name. Default: `{EntityName}Controller` |
| `serviceName` | `String` | `""` | No | Custom service class name. Default: `{EntityName}Service` |
| `repositoryName` | `String` | `""` | No | Custom repository interface name. Default: `{EntityName}Repository` |
| `packageName` | `String` | `""` | No | Package for generated classes. Default: entity's package |
| `customRepo` | `Class<?>` | `Void.class` | No | Use existing repository class instead of generating one |
| `customController` | `Class<?>` | `Void.class` | No | Use existing controller class instead of generating one |
| `customService` | `Class<?>` | `Void.class` | No | Use existing service class instead of generating one |
| `extendRepo` | `Class<?>` | `Void.class` | No | Interface for generated repository to extend |
| `extendController` | `Class<?>` | `Void.class` | No | Interface for generated controller to extend |
| `extendService` | `Class<?>` | `Void.class` | No | Interface for generated service to extend |
| `securityService` | `boolean` | `true` | No | Enable security service integration in generated controllers |
| `logging` | `boolean` | `true` | No | Enable debug logging in generated code |

### Generation Rules

The processor follows these rules to determine what to generate:

1. **Repository**: Always generated unless `customRepo` is specified
   - JPA: Extends `JpaRepository<Entity, IdType>` and `JpaSpecificationExecutor<Entity>`
   - PLAIN: Declares `findById`, `findAll`, `findAll(Pageable)`, `save`, `saveAll`, `deleteById`, `deleteAllById` plus any `@FindBy` / `@FindAllBy` methods (implement with any persistence technology)
   - MongoDB: Extends `MongoRepository<Entity, IdType>`

2. **Service**: Generated if:
   - `service = true` OR
   - `controllerPath` is not empty
   - Not generated if `customService` is specified

3. **Controller**: Generated only if:
   - `controllerPath` is not empty
   - `customController` is not specified

4. **DTOs**: Generated only if:
   - `controllerPath` is not empty
   - At least one field is annotated with `@DTOField` for each DTO type

5. **Mapper**: Generated only if:
   - `controllerPath` is not empty
   - `"Read"` DTO is specified

### ID Type Detection

The processor automatically detects the ID field by looking for fields annotated with `@Id` (from Spring Data). If no `@Id` field is found, it defaults to `Long`.

### Examples

```java
// Minimal - repository only
@CrudGen
@Entity
public class Product { ... }

// Repository + Service
@CrudGen(service = true)
@Entity
public class Order { ... }

// Full stack
@CrudGen(
    controllerPath = "/api/users",
    dtos = {"Read", "Create", "Update"}
)
@Entity
public class User { ... }

// MongoDB
@CrudGen(
    repo = RepoType.MONGO,
    controllerPath = "/api/documents",
    dtos = {"Read"}
)
@Document
public class Document { ... }

// Custom names and package
@CrudGen(
    controllerPath = "/api/products",
    dtos = {"Read", "Create"},
    controllerName = "ProductRestController",
    serviceName = "ProductBusinessService",
    packageName = "com.example.product.api"
)
@Entity
public class Product { ... }

// Extend interfaces
@CrudGen(
    controllerPath = "/api/users",
    dtos = {"Read"},
    extendRepo = CustomUserRepository.class,
    extendService = UserServiceInterface.class
)
@Entity
public class User { ... }
```

## @EndpointGen

Annotation for generating REST controllers from use case service classes.

### Target
- `ElementType.TYPE` - Applied to class declarations

### Parameters

| Parameter | Type | Default | Required | Description |
|-----------|------|---------|----------|-------------|
| `controllerPath` | `String` | - | **Yes** | REST controller base path (e.g., `/api/payments`) |
| `controllerName` | `String` | `""` | No | Custom controller class name. Default: `{ServiceName}Controller` |
| `packageName` | `String` | `""` | No | Package for generated controller. Default: service's package |
| `securityService` | `boolean` | `true` | No | Enable security service integration |
| `logging` | `boolean` | `true` | No | Enable debug logging |

### Generation Rules

- Controller is generated with `@Controller` annotation (not `@RestController`)
- All methods annotated with `@Endpoint` in the service class become REST endpoints
- Method parameters are preserved with their annotations (e.g., `@PathVariable`, `@RequestParam`)
- Return types are wrapped in `ResponseEntity` if not void

### Example

```java
@EndpointGen(controllerPath = "/api/orders")
public class OrderService {
    
    @Endpoint(method = HTTPMethod.POST, path = "/create")
    public Order createOrder(OrderRequest request) {
        // implementation
    }
    
    @Endpoint(method = HTTPMethod.GET, path = "/{id}")
    public Order getOrder(@PathVariable Long id) {
        // implementation
    }
    
    @Endpoint(method = HTTPMethod.DELETE, path = "/cancel/{id}")
    public void cancelOrder(@PathVariable Long id) {
        // implementation
    }
}
```

## @Endpoint

Annotation for marking methods in use case services that should become REST endpoints.

### Target
- `ElementType.METHOD` - Applied to method declarations

### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `method` | `HTTPMethod` | **Yes** | HTTP method: `GET`, `POST`, `PUT`, `PATCH`, or `DELETE` |
| `path` | `String` | **Yes** | Endpoint path relative to `controllerPath` from `@EndpointGen` |

### HTTP Methods

Available values from `HTTPMethod` enum:
- `GET` - Maps to `@GetMapping`
- `POST` - Maps to `@PostMapping`
- `PUT` - Maps to `@PutMapping`
- `PATCH` - Maps to `@PatchMapping`
- `DELETE` - Maps to `@DeleteMapping`

### Example

```java
@EndpointGen(controllerPath = "/api/payments")
public class PaymentService {
    
    @Endpoint(method = HTTPMethod.POST, path = "/process")
    public PaymentResult process(@RequestBody PaymentRequest request) {
        // implementation
    }
    
    @Endpoint(method = HTTPMethod.GET, path = "/status/{id}")
    public PaymentStatus getStatus(@PathVariable Long id) {
        // implementation
    }
}
```

## @DTOField

Annotation for marking entity fields to include in generated DTOs.

### Target
- `ElementType.FIELD` - Applied to field declarations

### Parameters

| Parameter | Type | Default | Required | Description |
|-----------|------|---------|----------|-------------|
| `dto` | `String` | - | **Yes** | DTO type: `"Read"`, `"Create"`, or `"Update"` |
| `fieldName` | `String` | `""` | No | Custom field name in DTO. Default: entity field name |

### Repeatable

This annotation is `@Repeatable`, so you can apply it multiple times to the same field for different DTO types.

### Validation Annotations

Jakarta Validation annotations (e.g., `@NotNull`, `@Size`, `@Email`) on entity fields are automatically copied to the corresponding DTO fields.

### Example

```java
@Entity
public class User {
    @Id
    private Long id;
    
    // Include in all DTOs
    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    @DTOField(dto = "Update")
    @NotNull
    @Size(min = 1, max = 100)
    private String name;
    
    // Only in Read and Create
    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    @Email
    private String email;
    
    // Custom field name in Update DTO
    @DTOField(dto = "Update", fieldName = "fullName")
    private String name;
}
```

## @FindBy

Annotation for generating a `findBy{FieldName}()` method in repository and service.

### Target
- `ElementType.FIELD` - Applied to field declarations

### Parameters
None

### Generated Methods

When applied to a field, the following methods are generated:

**In Repository:**
```java
EntityType findBy{FieldName}(FieldType fieldName);
```

**In Service:**
```java
EntityType findBy{FieldName}(FieldType fieldName);
```

**In Controller (if controller is generated):**
```java
GET /{controllerPath}/findBy{FieldName}?{fieldName}=value
```

### Example

```java
@Entity
public class User {
    @Id
    private Long id;
    
    @FindBy
    private String email;
}
```

Generates:
- `User findByEmail(String email)` in repository and service
- `GET /api/users/findByEmail?email=user@example.com` endpoint

## @FindAllBy

Annotation for generating `findAllBy{FieldName}()` methods in repository and service.

### Target
- `ElementType.FIELD` - Applied to field declarations

### Parameters
None

### Generated Methods

When applied to a field, the following methods are generated:

**In Repository:**
```java
List<EntityType> findAllBy{FieldName}(FieldType fieldName);
Page<EntityType> findAllBy{FieldName}(FieldType fieldName, Pageable pageable);
```

**In Service:**
```java
List<EntityType> findAllBy{FieldName}(FieldType fieldName);
Page<EntityType> findAllBy{FieldName}Paged(FieldType fieldName, Pageable pageable);
```

**In Controller (if controller is generated):**
```java
GET /{controllerPath}/findAllBy{FieldName}?{fieldName}=value
GET /{controllerPath}/findAllBy{FieldName}/paged?{fieldName}=value&page=0&size=20
```

### Example

```java
@Entity
public class Order {
    @Id
    private Long id;
    
    @FindAllBy
    private Long userId;
}
```

Generates:
- `List<Order> findAllByUserId(Long userId)` in repository and service
- `Page<Order> findAllByUserId(Long userId, Pageable pageable)` in repository
- `Page<Order> findAllByUserIdPaged(Long userId, Pageable pageable)` in service
- `GET /api/orders/findAllByUserId?userId=123` endpoint
- `GET /api/orders/findAllByUserId/paged?userId=123&page=0&size=20` endpoint

## Generated Classes Reference

### Repository Interface

Generated repository interfaces extend:
- **JPA**: `JpaRepository<Entity, IdType>` and `JpaSpecificationExecutor<Entity>`
- **MongoDB**: `MongoRepository<Entity, IdType>`
- **PLAIN**: No Spring Data supertype; the interface declares the same logical operations the generated service calls (see below).

Standard methods (either inherited from Spring Data **or** declared on **PLAIN**):
- `findById(IdType id)`
- `findAll()`
- `findAll(Pageable pageable)`
- `save(Entity entity)`
- `saveAll(Iterable<Entity> entities)`
- `deleteById(IdType id)`
- `deleteAllById(Iterable<IdType> ids)`

### Service Class

Generated service classes include:
- `get(IdType id)` - Get entity by ID, returns `null` if not found
- `getAll()` - Get all entities
- `getPaged(Pageable pageable)` - Get paginated entities
- `save(Entity entity)` - Save entity (transactional)
- `saveAll(List<Entity> entities)` - Save multiple entities (transactional)
- `delete(IdType id)` - Delete entity by ID (transactional)
- `deleteAll(List<IdType> ids)` - Delete multiple entities (transactional)
- Field-based query methods (if `@FindBy` or `@FindAllBy` annotations are present)

### Controller Class

Generated controllers include:
- Full CRUD endpoints (GET, POST, PATCH, DELETE)
- Batch operations (create, update, delete)
- Pagination support
- Field-based query endpoints
- Security integration (if enabled)
- Logging (if enabled)

### DTO Classes

Generated DTOs are immutable classes with:
- Private final fields
- Public constructor with all fields
- Public getter methods
- Jakarta Validation annotations (copied from entity fields)

### Mapper Interface

Generated mapper interfaces (MapStruct) include:
- `get(Entity entity)` - Map entity to Read DTO
- `create(CreateDTO dto)` - Map Create DTO to entity (if Create DTO exists)
- `toPatch(Entity entity)` - Map entity to Update DTO (if Update DTO exists)
- `patch(Entity entity, UpdateDTO dto)` - Apply Update DTO to entity (if Update DTO exists)

