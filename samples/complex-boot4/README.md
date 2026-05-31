# Maximal usage — Spring Boot 4

Same **maximal** library profile as [complex-boot3](../complex-boot3/): every annotation option, validated on Spring Boot 4.

## Stack

- Spring Boot **4.0.5**, `spring-boot-starter-webmvc`
- MapStruct **1.6.3**, zjsonpatch **0.6.2** (Jackson 3)
- H2 + mocked Mongo repository in tests (no real MongoDB)

Standalone `src/main` and `src/test` — not linked to complex-boot3.

## Boot 4 test wiring

Exclude Mongo autoconfig in the test application (`MongoAutoConfiguration`, `DataMongoAutoConfiguration`, `DataMongoRepositoriesAutoConfiguration`). Provide `@Primary` mock of generated `MongoTagRepository`. Exclude non-JPA `MongoTag` from JPA entity scan if needed.

Entity/feature matrix: same as [complex-boot3/README.md](../complex-boot3/README.md) (MegaProduct, ManualShelf, MongoTag, PlainCustomer, BespokeItem, HeadlessTask, FullHttpOps, EdgeOps).

## Tests

24 tests — same areas as complex-boot3.

## Docs

[docs/EXAMPLES.md](../../docs/EXAMPLES.md) §11, [docs/TROUBLESHOOTING.md](../../docs/TROUBLESHOOTING.md) (Boot 4 Mongo exclusions).
