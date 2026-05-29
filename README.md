# CRUDGen

Compile-time annotation processor for Spring Boot. Annotate an entity or a use-case class; CRUDGen generates repositories, services, REST controllers, DTOs, and MapStruct mappers.

## What you get

- **Entity CRUD** — `@CrudGen` on a JPA/Mongo entity (or `RepoType.PLAIN` contract)
- **Use-case HTTP** — `@EndpointGen` + `@Endpoint` on application services
- **Queries** — `@FindBy`, `@FindAllBy` on fields
- **Updates** — JSON Patch (`Update` DTO) with optional batch PATCH
- **Cross-cutting** — optional security hooks, lifecycle callbacks, OpenAPI annotations, debug logging

Generated code targets **Java 8** bytecode. Consumer apps run on **Spring Boot 3** (Jackson 2) or **Spring Boot 4** (Jackson 3).

## Install

**Gradle**

```kotlin
dependencies {
    compileOnly("io.github.bariskokulu:crudgen:1.1.0")
    annotationProcessor("org.projectlombok:lombok:1.18.42")           // if used
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final") // Boot 4: 1.6.3
    annotationProcessor("io.github.bariskokulu:crudgen:1.1.0")
}
```

**Maven** — `provided` dependency + `annotationProcessorPaths` (Lombok → MapStruct → CRUDGen).

See [Requirements](#requirements) for Spring, MapStruct, and JSON Patch versions.

## Minimal example

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

Generates repository, service, controller, `WidgetReadDTO`, `WidgetCreateDTO`, and `WidgetMapper`. Live copy: [`samples/simple-boot3`](samples/simple-boot3).

## Documentation

| Doc | Purpose |
|-----|---------|
| [docs/ANNOTATIONS.md](docs/ANNOTATIONS.md) | Every annotation parameter |
| [docs/DECISION-TREE.md](docs/DECISION-TREE.md) | What gets generated and when |
| [docs/EXAMPLES.md](docs/EXAMPLES.md) | Patterns with pointers to sample modules |
| [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) | Build/runtime failures |

## Sample modules

Four **standalone** Gradle projects under [`samples/`](samples/) — each owns its own sources and tests; Boot 3 and Boot 4 trees are **not** shared or synced.

| Module | Role |
|--------|------|
| [simple-boot3](samples/simple-boot3/) | Smallest useful HTTP CRUD |
| [simple-boot4](samples/simple-boot4/) | Same scenario on Boot 4 |
| [complex-boot3](samples/complex-boot3/) | Every processor feature + tests |
| [complex-boot4](samples/complex-boot4/) | Same matrix on Boot 4 |

```bash
./gradlew verifyAllExamples    # compile + test all four (Windows: gradlew.bat)
```

Details: [samples/README.md](samples/README.md).

## Requirements

**Consumer project**

- Java **8+** on the annotation-processor classpath; your app JDK is your choice.
- Spring Boot **3.2+** or **4.0+** with Web MVC and (for entities) JPA or Mongo **or** `RepoType.PLAIN` + your repository impl.
- **MapStruct** `1.5.5.Final` (Boot 3) or `1.6.3` (Boot 4).
- **JSON Patch** only if `Update` is in `dtos`: `zjsonpatch` `0.4.x` (Jackson 2) or `0.6.2+` (Jackson 3). Processor detects Jackson version from the compile classpath.
- Bean Validation: processor mirrors `javax.validation` or `jakarta.validation` from your compile classpath.

**Building this repo**

- Gradle **9.x**, Foojay toolchains.
- `lib` compiles with JDK **26**, `--release 8`.
- Sample modules use JDK **21** toolchain, `--release 17`.

## Security and lifecycle (optional)

When any annotated type keeps defaults (`securityService = true`, `lifecycleHooks = true`), the processor emits:

- `com.bariskokulu.crudgen.security.CrudGenSecurityService` — implement in your app
- `com.bariskokulu.crudgen.lifecycle.EntityLifecycleCallbacks<T>` — implement before/after hooks

Set both flags to `false` on **all** entities/use-cases if you do not need these interfaces.

## Publishing (maintainers)

`./gradlew :lib:publish` with GPG and Sonatype credentials in `gradle.properties` (see `gradle.properties.example`).

## License

Apache License 2.0 — [LICENSE](LICENSE).
