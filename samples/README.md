# Sample modules

These four Gradle projects exist to **prove CRUDGen works** at compile time and at runtime. They are not a shared library and they do not share source directories.

## Layout

```
samples/
├── simple-boot3/     Minimal HTTP CRUD (Boot 3.2.9)
├── simple-boot4/     Same intent (Boot 4.0.5)
├── complex-boot3/    Full processor surface (Boot 3.2.9)
└── complex-boot4/    Full processor surface (Boot 4.0.5)
```

Each module depends only on `:lib` (annotation processor) and its own Spring Boot BOM. **simple-boot3** and **simple-boot4** are separate file trees; **complex-boot3** and **complex-boot4** are separate file trees with the same feature matrix written twice.

## Which module to open

| Goal | Module |
|------|--------|
| “How little code do I need for a REST API?” | `simple-boot3` or `simple-boot4` |
| “Show me every annotation and option” | `complex-boot3` or `complex-boot4` |
| Boot 3 / Jackson 2 / `javax` or mixed validation | `*-boot3` |
| Boot 4 / Jackson 3 / modular autoconfigure | `*-boot4` |

## Verify everything

From repository root:

```bash
./gradlew verifyAllExamples
```

| Task | What it does |
|------|----------------|
| `compileAllExamples` | Processor runs; all four modules compile |
| `testAllExamples` | Spring Boot + H2 + MockMvc integration tests |
| `verifyAllExamples` | Both |

Windows: `gradlew.bat verifyAllExamples`

## Test counts (approx.)

| Module | Tests | Stack |
|--------|-------|-------|
| simple-boot3 | 7 | H2, MockMvc |
| simple-boot4 | 7 | H2, MockMvc |
| complex-boot3 | 19 | H2, mocked Mongo repo, MockMvc |
| complex-boot4 | 19 | H2, mocked Mongo repo, MockMvc |

Per-module READMEs describe entities, endpoints, and what each test class covers.
