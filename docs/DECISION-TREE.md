# What gets generated

CrudGen runs at compile time. Use this tree when choosing annotations before you write entities.

## `@CrudGen` on an entity

```
@Entity + @Id present?
  └─ NO → compile error

@CrudGen present?
  └─ NO → nothing generated

Repository
  customRepo specified?
    └─ YES → inject that Spring bean; no generated repository
              (need not extend Jpa/Mongo; must implement PLAIN CRUD/query methods)
    └─ NO  → generate {Entity}Repository (or repositoryName)
              extendRepo specified?
                └─ YES → generated repo extends **only** extendRepo
                         (must subtype JpaRepository/MongoRepository for JPA/MONGO)
                └─ NO  → repo = JPA   → JpaRepository + JpaSpecificationExecutor
                        repo = MONGO → MongoRepository
                        repo = PLAIN → @Repository interface with explicit CRUD + queries

Service
  customService specified?
    └─ YES → no generated service
    └─ NO  → generate if service=true OR controllerPath non-empty

Controller + DTOs + Mapper
  customController specified?
    └─ YES → no generated controller
              @DTOField(relation=true) → compile error unless you call
              {Entity}RelationApplier yourself
  controllerPath empty?
    └─ YES → no controller, no DTOs, no mapper
    └─ NO  → generate controller
              require "Read" in dtos
              generate DTOs listed in dtos (@DTOField per DTO per field)
              generate MapStruct mapper when Read DTO exists

Update / JSON Patch
  "Update" in dtos?
    └─ YES → PATCH /{id} and PATCH /batch; Jackson + zjsonpatch required at compile time
    └─ NO  → no PATCH endpoints

Field queries
  @FindBy on field     → findBy{Field}; GET …/findBy{Field} (404 if null)
  @FindAllBy on field  → findAllBy{Field} (+ paged endpoints)

Relations
  relation=true → FK ids in DTO; {Entity}RelationApplier + findById before save
  nestedRead=true (Read, scalar ref) → prefixed scalars from related Read DTO fields

Shared interfaces (once per compilation when needed)
  any @CrudGen / @EndpointGen with securityService=true (default)?
    └─ YES → CrudGenSecurityService
  any @CrudGen with lifecycleHooks=true (default)?
    └─ YES → EntityLifecycleCallbacks<T>
```

## `@EndpointGen` on a class

```
@EndpointGen present?
  └─ NO → nothing

controllerPath set (required)?
  └─ generate {Class}Controller under packageName or source package

Per @Endpoint method:
  HTTPMethod → Spring mapping annotation
  path starts with /
  each {name} in path matches a method parameter name
  void return → 204
  other returns → 200 in ResponseEntity

securityService / logging / openApi follow @EndpointGen (defaults true)
No EntityLifecycleCallbacks on use-case controllers
```

## Quick presets

| You want | Minimum annotation |
|----------|-------------------|
| Repository only | `@CrudGen` + `@Id` |
| Service, no HTTP | `@CrudGen(service = true)` |
| REST CRUD (minimal) | `controllerPath`, `dtos = {Read, Create}`, security/lifecycle off until beans exist |
| REST + JSON Patch | add `"Update"` to `dtos` + zjsonpatch |
| JDBC/MyBatis/custom store | `repo = PLAIN`, `customRepo = YourRepo.class` |
| MongoDB | `repo = MONGO` + Spring Data Mongo |
| Your REST for entity | `customController = X.class` (mind relations) |
| Use-case API | `@EndpointGen(controllerPath = "/api/…")` + `@Endpoint` |

Recipes: [EXAMPLES.md](EXAMPLES.md). Parameters: [ANNOTATIONS.md](ANNOTATIONS.md).
