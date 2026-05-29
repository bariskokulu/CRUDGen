# complex-boot3

Compile check that exercises **every `@CrudGen` / `@EndpointGen` / `@Endpoint` / `@DTOField` / `@FindBy` / `@FindAllBy` / `RepoType` value and boolean/Class/String attribute** the processor reads. JPA + Mongo + PLAIN; `customRepo` / `customService` / `customController`; `service` with empty `controllerPath`; `packageName` + non-default `repositoryName` / `serviceName` / `controllerName`; `extendRepo` (JPA base that already extends `JpaRepository` + `JpaSpecificationExecutor`) + `extendService` + `extendController`; `DTOField.fieldName`; Read/Create/Update + JSON Patch; all `HTTPMethod` enum values on `@Endpoint` (including `void` + optional `@RequestParam`); second `@EndpointGen` with `packageName` / `controllerName` overrides and flags off.

MapStruct emits **warnings** for unmapped `id` on create/patch and for `internalTitle` ↔ `displayTitle` because the processor does not emit `@Mapping`; that is expected for this stress module.

```bash
./gradlew :complex-boot3:compileJava
```
