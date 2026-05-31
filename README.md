# CrudGen

Compile-time annotation processor for Spring Boot 3 and 4. Annotate a JPA/Mongo entity or a use-case class; CrudGen generates repositories, services, REST controllers, immutable DTOs, and MapStruct mappers.

**Coordinates:** `io.github.bariskokulu:crudgen:1.1.0`  
**Bytecode:** Java 8 (your app JDK and Spring Boot version are independent).

## What the library does

| Area | Annotations |
|------|-------------|
| Entity CRUD | `@CrudGen` on a class with `@Id` |
| DTOs + validation mirror | `@DTOField` + `dtos = {Read, Create, Update}` |
| Field queries | `@FindBy`, `@FindAllBy` |
| JSON Patch updates | `"Update"` in `dtos` + zjsonpatch on compile classpath |
| FK relations in API | `@DTOField(relation = true)` + `{Entity}RelationApplier` |
| Persistence backends | `RepoType.JPA`, `MONGO`, `PLAIN` |
| Custom / extended layers | `customRepo`, `customService`, `customController`, `extend*` |
| Use-case HTTP | `@EndpointGen` + `@Endpoint` |
| Cross-cutting | `CrudGenSecurityService`, `EntityLifecycleCallbacks` (optional) |

## Install

**Gradle (Kotlin DSL)**

```kotlin
dependencies {
    compileOnly("io.github.bariskokulu:crudgen:1.1.0")
    annotationProcessor("org.projectlombok:lombok:1.18.42")           // if used
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final") // Boot 4: 1.6.3
    annotationProcessor("io.github.bariskokulu:crudgen:1.1.0")
}
```

**Maven** — `provided` dependency + `annotationProcessorPaths`: **Lombok → MapStruct → CrudGen**.

## Minimal usage

```java
@Entity
@CrudGen(
    controllerPath = "/api/widgets",
    dtos = { "Read", "Create" },
    securityService = false,
    lifecycleHooks = false,
    openApi = false,
    logging = false
)
public class Widget {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DTOField(dto = "Read")
    @DTOField(dto = "Create")
    @NotBlank
    private String name;
}
```

Generates repository, service, controller, Read/Create DTOs, and mapper. Step-by-step recipes: [docs/EXAMPLES.md](docs/EXAMPLES.md).

## Documentation

| Doc | Content |
|-----|---------|
| [docs/EXAMPLES.md](docs/EXAMPLES.md) | Minimal → full usage (inline code) |
| [docs/ANNOTATIONS.md](docs/ANNOTATIONS.md) | Every annotation parameter |
| [docs/DECISION-TREE.md](docs/DECISION-TREE.md) | What gets generated and when |
| [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) | Build and runtime failures |

Agent skills (Cursor + Claude): `.cursor/skills/crudgen/SKILL.md` and `.claude/skills/crudgen/SKILL.md` — keep identical when the processor changes.

## Requirements

**Your application**

- Java **8+** for compiling against the processor; runtime JDK is your choice.
- Spring Boot **3.2+** or **4.0+** with Web MVC and (for entities) Spring Data JPA or Mongo, or `RepoType.PLAIN` with your repository implementation.
- **MapStruct** `1.5.5.Final` (Boot 3) or `1.6.3` (Boot 4) when `controllerPath` is set.
- **JSON Patch** only if `Update` is in `dtos`: zjsonpatch `0.4.x` (Jackson 2) or `0.6.2+` (Jackson 3).
- Bean Validation: processor mirrors `javax.validation` or `jakarta.validation` from your compile classpath.

| Stack | Web starter | MapStruct | zjsonpatch (Update DTO) |
|-------|-------------|-----------|-------------------------|
| Boot 3 | `spring-boot-starter-web` | 1.5.5.Final | `com.flipkart.zjsonpatch:zjsonpatch:0.4.16` |
| Boot 4 | `spring-boot-starter-webmvc` | 1.6.3 | `io.github.vishwakarma:zjsonpatch:0.6.2` |

## Security and lifecycle (optional)

Defaults: `securityService = true`, `lifecycleHooks = true`. The processor may emit:

- `com.bariskokulu.crudgen.security.CrudGenSecurityService`
- `com.bariskokulu.crudgen.lifecycle.EntityLifecycleCallbacks<T>`

Implement in your application, or set both flags to `false` on **every** `@CrudGen` / `@EndpointGen` type so the interfaces are not generated.

## License

Apache License 2.0 — [LICENSE](LICENSE).
