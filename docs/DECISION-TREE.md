# What gets generated

CRUDGen runs at compile time. Use this tree to decide annotations before you write entities.

## `@CrudGen` on an entity

```
@Entity + @Id present?
  └─ NO → compile error

@CrudGen present?
  └─ NO → nothing generated

Repository
  customRepo specified?
    └─ YES → use that type; no generated repository interface
    └─ NO  → generate {Entity}Repository (or repositoryName)
              repo = JPA   → extends JpaRepository + JpaSpecificationExecutor
              repo = MONGO → extends MongoRepository
              repo = PLAIN → @Repository interface with explicit CRUD + query methods

Service
  customService specified?
    └─ YES → no generated service
    └─ NO  → generate if service=true OR controllerPath non-empty

Controller + DTOs + Mapper
  customController specified?
    └─ YES → no generated controller (service/repo/DTOs/mapper still follow rules below)
  controllerPath empty?
    └─ YES → no controller, no DTOs, no mapper
    └─ NO  → generate controller
              require "Read" in dtos
              generate DTOs listed in dtos (fields need @DTOField per DTO)
              generate MapStruct mapper when Read DTO exists

Update / JSON Patch
  "Update" in dtos?
    └─ YES → PATCH /{id} and PATCH /batch; require Jackson + zjsonpatch on classpath
    └─ NO  → no PATCH endpoints; no ObjectMapper/Validator in controller ctor

Field queries
  @FindBy on field     → findBy{Field} in repo/service; GET …/findBy{Field}
  @FindAllBy on field  → findAllBy{Field} (+ paged); GET …/findAllBy{Field}[/paged]

Shared interfaces (once per compilation round)
  any entity/use-case with securityService=true (default)?
    └─ YES → generate CrudGenSecurityService
  any entity with lifecycleHooks=true (default)?
    └─ YES → generate EntityLifecycleCallbacks<T>
```

## `@EndpointGen` on a class

```
@EndpointGen present?
  └─ NO → nothing

controllerPath set (required)?
  └─ generate {Class}Controller (or controllerName) under packageName or source package

For each @Endpoint method:
  map HTTPMethod → @GetMapping / @PostMapping / …
  path must start with /
  each {name} in path must match a method parameter name
  void return → 204 No Content
  other returns → 200 OK in ResponseEntity

securityService / logging / openApi follow @EndpointGen flags (defaults true)
```

## Quick presets

| You want | Minimum annotation |
|----------|-------------------|
| Repository only | `@CrudGen` + `@Id` |
| Service, no HTTP | `@CrudGen(service = true)` |
| REST CRUD | `@CrudGen(controllerPath = "/api/x", dtos = {"Read", "Create"})` |
| + JSON Patch updates | add `"Update"` to `dtos` + zjsonpatch dep |
| JDBC/MyBatis/etc. | `@CrudGen(repo = PLAIN, customRepo = YourRepo.class)` |
| MongoDB | `@CrudGen(repo = MONGO, …)` + Spring Data Mongo |
| Custom REST for entity | `@CrudGen(customController = X.class, controllerPath = "…")` |
| Use-case API | `@EndpointGen(controllerPath = "/api/…")` + `@Endpoint` on methods |

Live presets: [samples/simple-boot3](../samples/simple-boot3) (minimal), [samples/complex-boot3](../samples/complex-boot3) (full).

Parameter details: [ANNOTATIONS.md](ANNOTATIONS.md).
