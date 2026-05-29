# Examples

## Basic CRUD Operations

### Full Stack Generation

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
    @NotNull
    @Size(min = 1, max = 100)
    private String name;
    
    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    @Email
    private String email;
    
    // Not included in DTOs
    private String passwordHash;
    
    @DTOField(dto = "Read")
    private LocalDateTime createdAt;
}
```

**Generated Files:**
- `UserRepository` - JPA repository
- `UserService` - Service with CRUD methods
- `UserController` - REST controller with endpoints:
  - `GET /api/users/{id}`
  - `GET /api/users/`
  - `GET /api/users/paged?page=0&size=20`
  - `POST /api/users`
  - `POST /api/users/batch`
  - `PATCH /api/users/{id}` (JSON Patch)
  - `PATCH /api/users/batch` (JSON Patch)
  - `DELETE /api/users/{id}`
  - `DELETE /api/users/batch`
- `UserReadDTO`, `UserCreateDTO`, `UserUpdateDTO`
- `UserMapper` - MapStruct interface

## Repository Only

Generate only the repository interface:

```java
@CrudGen
@Entity
public class Product {
    @Id
    private Long id;
    
    private String name;
    private BigDecimal price;
    private Integer stock;
}
```

**Generated Files:**
- `ProductRepository` - JPA repository extending `JpaRepository<Product, Long>` and `JpaSpecificationExecutor<Product>`

## Service + Repository

Generate repository and service, but no controller:

```java
@CrudGen(service = true)
@Entity
public class Order {
    @Id
    private Long id;
    
    private Long userId;
    private BigDecimal total;
    private OrderStatus status;
}
```

**Generated Files:**
- `OrderRepository` - JPA repository
- `OrderService` - Service class with CRUD methods

You can inject and use the service in your own controllers or other services.

## MongoDB Repository

Generate MongoDB repository instead of JPA:

```java
@CrudGen(
    repo = RepoType.MONGO,
    controllerPath = "/api/documents",
    dtos = {"Read", "Create"}
)
@Document
public class Document {
    @Id
    private String id;  // MongoDB uses String IDs
    
    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    private String title;
    
    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    private String content;
}
```

**Generated Files:**
- `DocumentRepository` - MongoDB repository extending `MongoRepository<Document, String>`
- `DocumentService` - Service with CRUD methods
- `DocumentController` - REST controller
- `DocumentReadDTO`, `DocumentCreateDTO`
- `DocumentMapper` - MapStruct interface

## Custom Names and Packages

Customize generated class names and package:

```java
@CrudGen(
    controllerPath = "/api/products",
    dtos = {"Read", "Create", "Update"},
    controllerName = "ProductRestController",
    serviceName = "ProductBusinessService",
    repositoryName = "ProductDataRepository",
    packageName = "com.example.product.api"
)
@Entity
public class Product {
    @Id
    private Long id;
    
    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    @DTOField(dto = "Update")
    private String name;
}
```

**Generated Files (in `com.example.product.api` package):**
- `ProductDataRepository`
- `ProductBusinessService`
- `ProductRestController`
- `ProductReadDTO`, `ProductCreateDTO`, `ProductUpdateDTO`
- `ProductMapper`

## Extending Interfaces

Extend custom interfaces for generated classes:

```java
// Custom repository interface
public interface CustomUserRepository {
    List<User> findActiveUsers();
}

// Custom service interface
public interface UserServiceInterface {
    User activateUser(Long id);
}

@CrudGen(
    controllerPath = "/api/users",
    dtos = {"Read"},
    extendRepo = CustomUserRepository.class,
    extendService = UserServiceInterface.class
)
@Entity
public class User {
    @Id
    private Long id;
    
    @DTOField(dto = "Read")
    private String name;
}
```

**Generated Files:**
- `UserRepository` extends `JpaRepository<User, Long>`, `JpaSpecificationExecutor<User>`, and `CustomUserRepository`
- `UserService` implements `UserServiceInterface` (you need to implement `activateUser` method)

## Field-Based Queries

Generate query methods based on field annotations:

```java
@CrudGen(
    controllerPath = "/api/users",
    dtos = {"Read"}
)
@Entity
public class User {
    @Id
    private Long id;
    
    @FindBy
    @DTOField(dto = "Read")
    private String email;  // Generates findByEmail()
    
    @FindAllBy
    @DTOField(dto = "Read")
    private String department;  // Generates findAllByDepartment()
    
    @DTOField(dto = "Read")
    private String name;
}
```

**Generated Repository Methods:**
```java
User findByEmail(String email);
List<User> findAllByDepartment(String department);
Page<User> findAllByDepartment(String department, Pageable pageable);
```

**Generated Service Methods:**
```java
User findByEmail(String email);
List<User> findAllByDepartment(String department);
Page<User> findAllByDepartmentPaged(String department, Pageable pageable);
```

**Generated Controller Endpoints:**
- `GET /api/users/findByEmail?email=user@example.com`
- `GET /api/users/findAllByDepartment?department=Engineering`
- `GET /api/users/findAllByDepartment/paged?department=Engineering&page=0&size=20`

## Use Case Controllers

Generate REST controllers for use case services:

```java
@EndpointGen(controllerPath = "/api/payments")
public class PaymentService {
    
    @Endpoint(method = HTTPMethod.POST, path = "/process")
    public PaymentResult processPayment(@RequestBody PaymentRequest request) {
        // Your implementation
        return paymentProcessor.process(request);
    }
    
    @Endpoint(method = HTTPMethod.GET, path = "/status/{id}")
    public PaymentStatus getPaymentStatus(@PathVariable Long id) {
        // Your implementation
        return paymentRepository.getStatus(id);
    }
    
    @Endpoint(method = HTTPMethod.GET, path = "/history")
    public List<Payment> getPaymentHistory(
        @RequestParam Long userId,
        @RequestParam(required = false) LocalDate fromDate
    ) {
        // Your implementation
        return paymentRepository.findByUserIdAndDate(userId, fromDate);
    }
    
    @Endpoint(method = HTTPMethod.DELETE, path = "/cancel/{id}")
    public void cancelPayment(@PathVariable Long id) {
        // Your implementation
        paymentProcessor.cancel(id);
    }
}
```

**Generated Controller:**
```java
@Controller
@RequestMapping("/api/payments")
public class PaymentServiceController {
    private final PaymentService service;
    private final CrudGenSecurityService securityService;  // if security enabled
    
    // Constructor and endpoints...
}
```

**Generated Endpoints:**
- `POST /api/payments/process`
- `GET /api/payments/status/{id}`
- `GET /api/payments/history?userId=123&fromDate=2024-01-01`
- `DELETE /api/payments/cancel/{id}`

## Security Integration

Implement security service for access control:

```java
@Service
public class MySecurityService implements CrudGenSecurityService {
    
    @Override
    public void checkEntityAccess(String entityClassName, String method, Object... params) {
        // Check if current user can perform method on entity
        String currentUser = getCurrentUser();
        
        switch (method) {
            case "get":
            case "getAll":
            case "getPaged":
                // Check read permission
                if (!hasPermission(currentUser, entityClassName, "READ")) {
                    throw new AccessDeniedException("No read permission");
                }
                break;
            case "create":
            case "createBatch":
                // Check create permission
                if (!hasPermission(currentUser, entityClassName, "CREATE")) {
                    throw new AccessDeniedException("No create permission");
                }
                break;
            case "update":
            case "updateBatch":
                // Check update permission
                if (!hasPermission(currentUser, entityClassName, "UPDATE")) {
                    throw new AccessDeniedException("No update permission");
                }
                break;
            case "delete":
            case "deleteBatch":
                // Check delete permission
                if (!hasPermission(currentUser, entityClassName, "DELETE")) {
                    throw new AccessDeniedException("No delete permission");
                }
                break;
        }
    }
    
    @Override
    public void checkUseAccess(String method, Object... params) {
        // Check if current user can call use case method
        String currentUser = getCurrentUser();
        if (!hasPermission(currentUser, method, "EXECUTE")) {
            throw new AccessDeniedException("No permission to execute " + method);
        }
    }
    
    private String getCurrentUser() {
        // Get current authenticated user
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    
    private boolean hasPermission(String user, String resource, String action) {
        // Your permission checking logic
        return true;  // Implement your logic
    }
}
```

Disable security for specific entities:

```java
@CrudGen(
    controllerPath = "/api/public/products",
    dtos = {"Read"},
    securityService = false  // No security checks
)
@Entity
public class Product {
    // ...
}
```

## Advanced DTO Configuration

### Different Fields in Different DTOs

```java
@CrudGen(
    controllerPath = "/api/users",
    dtos = {"Read", "Create", "Update"}
)
@Entity
public class User {
    @Id
    private Long id;
    
    // In all DTOs
    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    @DTOField(dto = "Update")
    private String name;
    
    // Only in Read and Create (not in Update - email can't be changed)
    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    @Email
    private String email;
    
    // Only in Create (password never returned or updated via API)
    @DTOField(dto = "Create")
    @Size(min = 8)
    private String password;
    
    // Only in Read (auto-generated, not in Create/Update)
    @DTOField(dto = "Read")
    private LocalDateTime createdAt;
    
    @DTOField(dto = "Read")
    private LocalDateTime updatedAt;
    
    // Internal field, not in any DTO
    private String passwordHash;
}
```

### Custom Field Names in DTOs

```java
@Entity
public class User {
    @Id
    private Long id;
    
    // Field named "name" in entity, but "fullName" in Update DTO
    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    @DTOField(dto = "Update", fieldName = "fullName")
    private String name;
}
```

This generates:
- `UserReadDTO` with field `name`
- `UserCreateDTO` with field `name`
- `UserUpdateDTO` with field `fullName`

### Validation Annotations

Jakarta Validation annotations are automatically copied to DTO fields:

```java
@Entity
public class User {
    @Id
    private Long id;
    
    @DTOField(dto = "Create")
    @DTOField(dto = "Update")
    @NotNull
    @Size(min = 1, max = 100)
    private String name;
    
    @DTOField(dto = "Create")
    @Email
    @NotBlank
    private String email;
    
    @DTOField(dto = "Create")
    @Min(18)
    @Max(120)
    private Integer age;
}
```

The generated DTOs will include these validation annotations, and the controller will validate request bodies automatically.

## Complete Example: E-Commerce Application

```java
// Product Entity
@CrudGen(
    controllerPath = "/api/products",
    dtos = {"Read", "Create", "Update"},
    service = true
)
@Entity
public class Product {
    @Id
    private Long id;
    
    @FindBy
    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    @DTOField(dto = "Update")
    @NotNull
    @Size(min = 1, max = 200)
    private String name;
    
    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    @DTOField(dto = "Update")
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;
    
    @FindAllBy
    @DTOField(dto = "Read")
    private Long categoryId;
    
    @DTOField(dto = "Read")
    private Integer stock;
}

// Order Entity (Repository + Service only)
@CrudGen(service = true)
@Entity
public class Order {
    @Id
    private Long id;
    
    private Long userId;
    private BigDecimal total;
    private OrderStatus status;
    private LocalDateTime createdAt;
}

// Payment Use Case Service
@EndpointGen(controllerPath = "/api/payments")
public class PaymentService {
    
    @Endpoint(method = HTTPMethod.POST, path = "/process")
    public PaymentResult processPayment(@RequestBody PaymentRequest request) {
        // Implementation
    }
    
    @Endpoint(method = HTTPMethod.GET, path = "/status/{id}")
    public PaymentStatus getStatus(@PathVariable Long id) {
        // Implementation
    }
}
```

This setup generates:
- Full CRUD API for products with field-based queries
- Service layer for orders (no controller)
- Use case controller for payments

