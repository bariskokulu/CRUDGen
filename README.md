# CrudGen

Compile-time annotation processor for Spring Boot 3 and 4. Annotate a JPA/Mongo entity or a use-case class; CrudGen generates repositories, services, REST controllers, immutable DTOs, and MapStruct mappers.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.bariskokulu/crudgen.svg)](https://central.sonatype.com/artifact/io.github.bariskokulu/crudgen)

## Features

- **Compile-time generation** — The processor runs in the `javac` annotation-processing round and emits plain `.java` sources. No runtime bytecode weaving, no reflection-based code paths in generated controllers or services.
- **N+1-safe JPA reads** — When `RepoType.JPA` is used and the Read DTO includes `relation=true` fields, generated repositories expose `@EntityGraph` read methods; services call them on `get` / `getAll` / `getPaged`. Relation binding stays out of MapStruct mappers.
- **JSON Patch updates** — With `"Update"` in `dtos`, generated controllers expose `PATCH /{id}` and `PATCH /batch` consuming `application/json-patch+json`, applying patches via zjsonpatch and validating the merged DTO.
- **Configurable surface per type** — `dtos`, `controllerPath`, `service`, `extend*` / `custom*`, and `@EndpointGen` control which layers and endpoints are generated. Empty `controllerPath` skips controller, DTOs, and mapper; `customRepo` / `customService` / `customController` swap in your implementations.

## Quick comparison (before / after)

### Before — annotated entity only

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
    @NotBlank @Size(max = 200)
    private String name;
}
```

### After — one `compileJava` pass

```
com.example.app/
├── WidgetRepository.java      # JpaRepository (+ @FindBy / @FindAllBy when declared)
├── WidgetService.java         # @Service, @Transactional mutations
├── WidgetController.java      # @RestController under controllerPath
├── WidgetReadDTO.java         # immutable; Bean Validation mirrored from entity fields
├── WidgetCreateDTO.java
├── WidgetMapper.java          # MapStruct @Mapper(componentModel = "spring")
└── WidgetMapperImpl.java      # MapStruct annotation-processor output
```

Representative generated code:

```java
@Repository
public interface WidgetRepository extends JpaRepository<Widget, Long>, JpaSpecificationExecutor<Widget> {}

@Service
public class WidgetService {
  public Widget get(Long id) { return repo.findById(id).orElse(null); }
  @Transactional public Widget create(Widget entity) { return repo.save(entity); }
}

@RestController
@RequestMapping("/api/widgets")
public class WidgetController {
  @GetMapping("/{id}")
  public ResponseEntity<WidgetReadDTO> get(@PathVariable Long id) {
    Widget entity = service.get(id);
    if (entity == null) throw new ResponseStatusException(NOT_FOUND, "Entity with id " + id + " not found.");
    return ResponseEntity.ok(mapper.get(entity));
  }
  @PostMapping("/")
  public ResponseEntity<WidgetReadDTO> create(@Valid @RequestBody WidgetCreateDTO dto) { /* ... */ }
}

@Mapper(componentModel = "spring")
public interface WidgetMapper {
  WidgetReadDTO get(Widget entity);
  Widget create(WidgetCreateDTO dto);
}

public class WidgetReadDTO {
  @NotBlank @Size(max = 200)
  private final String name;
  @JsonCreator
  public WidgetReadDTO(@JsonProperty("name") String name) { this.name = name; }
}
```

**Coordinates:** `io.github.bariskokulu:crudgen:1.1.1`  
**Bytecode:** Java 17 (your app JDK and Spring Boot version are independent).

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
    compileOnly("io.github.bariskokulu:crudgen:1.1.1")
    annotationProcessor("org.projectlombok:lombok:1.18.42")           // if used
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final") // Boot 4: 1.6.3
    annotationProcessor("io.github.bariskokulu:crudgen:1.1.1")
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

- Java **17+** for compiling against the processor; runtime JDK is your choice.
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
