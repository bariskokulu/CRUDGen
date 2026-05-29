# Troubleshooting

Common issues and solutions when using CRUDGen.

## Table of Contents

1. [Compilation Issues](#compilation-issues)
2. [Generated Classes Not Found](#generated-classes-not-found)
3. [Missing Dependencies](#missing-dependencies)
4. [Security Service Issues](#security-service-issues)
5. [Conditional stubs (security / lifecycle)](#conditional-stubs-security--lifecycle)
6. [DTO Generation Issues](#dto-generation-issues)
7. [Mapper Issues](#mapper-issues)
8. [Validation Issues](#validation-issues)
9. [Incremental Processing](#incremental-processing)

## Compilation Issues

### Error: "A 'Read' DTO is required for entity X"

**Problem:** You've set `controllerPath` but haven't specified `"Read"` in the `dtos` array.

**Solution:**
```java
@CrudGen(
    controllerPath = "/api/users",
    dtos = {"Read"}  // Add "Read" to dtos array
)
@Entity
public class User {
    // ...
}
```

### Error: "Controller path must start with '/' or be empty"

**Problem:** The `controllerPath` doesn't start with `/`.

**Solution:**
```java
@CrudGen(
    controllerPath = "/api/users",  // Must start with /
    dtos = {"Read"}
)
```

### Error: "Invalid controller name", "Invalid service name", or "Invalid repository name"

**Problem:** The custom name you provided is not a valid Java identifier.

**Solution:** Use valid Java identifiers (letters, digits, underscore, starting with letter):
```java
@CrudGen(
    controllerPath = "/api/users",
    dtos = {"Read"},
    controllerName = "UserRestController",  // Valid identifier
    // controllerName = "123Invalid"  // Invalid - starts with digit
)
```

### Error: "Invalid package name"

**Problem:** The `packageName` contains invalid characters or structure.

**Solution:** Use valid Java package names:
```java
@CrudGen(
    controllerPath = "/api/users",
    dtos = {"Read"},
    packageName = "com.example.api",  // Valid package name
    // packageName = "com.example.api."  // Invalid - trailing dot
)
```

## Generated Classes Not Found

### IDE doesn't see generated classes

**Problem:** Your IDE hasn't indexed the generated sources.

**Solutions:**

1. **Rebuild the project:**
   - Maven: `mvn clean compile`
   - Gradle: `./gradlew clean build`

2. **Refresh IDE:**
   - **IntelliJ IDEA:**
     - File → Invalidate Caches / Restart
     - Or: Build → Rebuild Project
   - **Eclipse:**
     - Project → Clean
     - Project → Build Project

3. **Check annotation processing:**
   - **IntelliJ IDEA:**
     - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
     - Enable annotation processing
   - **Eclipse:**
     - Project Properties → Java Compiler → Annotation Processing
     - Enable annotation processing

4. **Check generated sources directory:**
   - Generated files should be in `target/generated-sources/annotations` (Maven) or `build/generated/sources/annotationProcessor` (Gradle)
   - Ensure this directory is marked as a source folder in your IDE

### Generated classes not compiling

**Problem:** Missing required dependencies in your project.

**Solution:** Ensure you have all required dependencies. See [Missing Dependencies](#missing-dependencies).

## Missing Dependencies

### Error: Cannot resolve symbol 'ResponseEntity', 'Page', etc.

**Problem:** Spring Boot dependencies are missing.

**Solution:** Add Spring Boot Web dependency:

**Maven:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**Gradle:**
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
}
```

### Error: Cannot resolve symbol 'JpaRepository', 'MongoRepository'

**Problem:** Spring Data dependencies are missing.

**Solution:** Add appropriate Spring Data dependency:

**For JPA:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

**For MongoDB:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

### Error: Cannot resolve symbol 'Mapper', 'BeanMapping', etc.

**Problem:** MapStruct is missing.

**Solution:** Add MapStruct dependency and annotation processor:

**Maven:**
```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>
```

**Gradle:**
```kotlin
dependencies {
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
}
```

### Error: Cannot resolve symbol 'JsonPatch', 'JsonNode'

**Problem:** The entity lists **`Update`** in `@CrudGen(dtos = …)` but Jackson databind or zjsonpatch is not on the **compile** classpath, or the processor reported a missing type during processing.

**If you do not want JSON Patch:** remove **`Update`** from `dtos` (and matching `@DTOField`); generated controllers will not reference `JsonPatch` / `ObjectMapper`.

**If you want JSON Patch:** add dependencies:

**Maven (Spring Boot 3 / Jackson 2):**
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>com.flipkart.zjsonpatch</groupId>
    <artifactId>zjsonpatch</artifactId>
    <version>0.4.16</version>
</dependency>
```

**Gradle (Spring Boot 3 / Jackson 2):**
```kotlin
dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.flipkart.zjsonpatch:zjsonpatch:0.4.16")
}
```

**Spring Boot 4 / Jackson 3:** **`io.github.vishwakarma:zjsonpatch:0.6.2+`** and Boot 4’s Jackson 3 BOM. The processor switches to **`Jackson3JsonPatch`** and **`tools.jackson.*`** only when **`tools.jackson.databind.ObjectMapper`** is on the **javac compile classpath** (normal with `spring-boot-starter-web` / json). If your IDE or build hides Jackson from that classpath, fix the compile classpath so the processor sees the same types as the compiler.

Note: Jackson is usually included with Spring Boot Web, but zjsonpatch must be declared explicitly. **`com.flipkart.zjsonpatch`** coordinates top out at **0.4.16** (Jackson 2); Jackson 3 builds use **`io.github.vishwakarma:zjsonpatch`**.

## Security Service Issues

### Error: Cannot resolve symbol 'CrudGenSecurityService'

**Problem:** The security service interface hasn't been generated yet, or you need to implement it.

**Solution:**

1. **Ensure the interface is generated:**
   - The interface is generated in package `com.bariskokulu.crudgen.security` **only when** at least one `@CrudGen` or `@EndpointGen` has `securityService = true` (the default).
   - If every annotated type uses `securityService = false`, the stub is **not** emitted; nothing to implement.
   - Rebuild (prefer `clean`) after changing flags so stale files under `build/generated` do not linger.

2. **Implement the interface:**
```java
@Service
public class MySecurityService implements CrudGenSecurityService {
    @Override
    public void checkEntityAccess(String entityClassName, String method, Object... params) {
        // Your security logic
    }
    
    @Override
    public void checkUseAccess(String method, Object... params) {
        // Your security logic
    }
}
```

3. **Disable security if not needed:**
```java
@CrudGen(
    controllerPath = "/api/users",
    dtos = {"Read"},
    securityService = false  // Disable security
)
```

## Conditional stubs (security / lifecycle)

### `CrudGenSecurityService` or `EntityLifecycleCallbacks` missing after upgrade

**Problem:** You relied on those types always being generated; the processor now emits them only when needed.

**Solution:**

- **`CrudGenSecurityService`:** Set `securityService = true` on at least one `@CrudGen` / `@EndpointGen`, or add your own interface in that package if you prefer a fixed API.
- **`EntityLifecycleCallbacks`:** Set `lifecycleHooks = true` on at least one `@CrudGen`, or stop referencing the type if you disabled lifecycle hooks everywhere. Implement `beforeCreate`, `afterCreate`, `beforeUpdate`, `afterUpdate`, `beforeDelete`, `afterDelete`; override `*Batch` defaults only if you need batch hooks.

Run **`clean compile`** after toggling these flags.

### Security service not being called

**Problem:** Security service implementation is not being injected.

**Solution:**
- Ensure your security service is annotated with `@Service` or `@Component`
- Ensure it's in a package scanned by Spring (`@ComponentScan`)
- Check that `securityService = true` in your `@CrudGen` annotation

## DTO Generation Issues

### DTO has no fields

**Problem:** No entity fields are annotated with `@DTOField` for the DTO type you're generating.

**Solution:** Annotate at least one field:
```java
@Entity
public class User {
    @Id
    private Long id;
    
    @DTOField(dto = "Read")  // Add this annotation
    private String name;
}
```

### DTO field missing validation annotations

**Problem:** Validation annotations on entity fields aren't being copied to DTOs.

**Solution:** Ensure annotations are from `jakarta.validation` package:
```java
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class User {
    @DTOField(dto = "Create")
    @NotNull  // Jakarta Validation annotation
    @Size(min = 1, max = 100)
    private String name;
}
```

### Custom field name not working

**Problem:** The `fieldName` parameter in `@DTOField` isn't being used.

**Solution:** Check the annotation syntax:
```java
@DTOField(dto = "Update", fieldName = "fullName")  // Correct
private String name;
```

## Mapper Issues

### MapStruct mapper implementation not generated

**Problem:** MapStruct annotation processor is not configured correctly.

**Solution:**

1. **Ensure MapStruct processor is in annotation processor path:**
   - Maven: Should be automatic if in dependencies
   - Gradle: Use `annotationProcessor` configuration

2. **Check MapStruct version compatibility:**
   - Use MapStruct 1.5.x or later

3. **Ensure mapper interface is in a package scanned by MapStruct:**
   - MapStruct needs to find the generated mapper interface
   - Usually works if in the same module

4. **Rebuild the project:**
   - Clean and rebuild to trigger MapStruct processing

### Mapper methods not found

**Problem:** The mapper interface is generated but methods are missing.

**Solution:**
- Ensure `"Read"` DTO is specified (required for `get` method)
- Ensure `"Create"` DTO is specified for `create` method
- Ensure `"Update"` DTO is specified for `toPatch` and `patch` methods

## Validation Issues

### Validation not working in controller

**Problem:** Jakarta Validation is not on the classpath or not configured.

**Solution:**

1. **Add validation dependency:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

2. **Ensure `@Valid` annotation is used:**
   - The generated controller automatically uses `@Valid` on request bodies
   - This should work out of the box

### Constraint violations not being caught

**Problem:** Exception handler for validation is missing.

**Solution:** Add a global exception handler:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(field, message);
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
```

## Incremental Processing

### Changes not reflected after modification

**Problem:** Incremental processing might be caching old results.

**Solution:**

1. **Clean build:**
   - Maven: `mvn clean compile`
   - Gradle: `./gradlew clean build`

2. **Check incremental processing:**
   - The processor supports incremental processing (marked as "isolating")
   - If issues persist, try a clean build

3. **Invalidate IDE caches:**
   - IntelliJ: File → Invalidate Caches / Restart

### Generated files from previous build interfering

**Problem:** Old generated files are causing conflicts.

**Solution:**
- Clean the build directory
- Delete generated sources directory
- Rebuild the project

## General Tips

1. **Always rebuild after changing annotations:**
   - Annotation processing happens during compilation
   - IDE auto-compile might not catch all changes

2. **Check generated sources:**
   - Look at the generated files to understand what was created
   - Generated files are in standard generated sources directories

3. **Enable annotation processing:**
   - Ensure annotation processing is enabled in your IDE
   - Check compiler settings

4. **Check Java version:**
   - The published **processor JAR** targets **Java 8** bytecode (`--release 8`); run it with a JDK your build tool supports (typically **17+** for Gradle 9).
   - **This repo** uses Gradle toolchains (**JDK 26** for `lib`, **JDK 21** for example modules); match or override toolchains if you build from source.

5. **Verify dependencies:**
   - All required dependencies must be on the classpath
   - Check both compile-time and runtime dependencies

## Getting Help

If you encounter issues not covered here:

1. Check the generated source files to understand what was created
2. Review the [Annotation Reference](ANNOTATIONS.md) for correct usage
3. Look at the [Examples](EXAMPLES.md) for usage patterns
4. Check that all required dependencies are present
5. Ensure annotation processing is enabled and working

