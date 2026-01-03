**ALWAYS** add the STARTER_CHARACTER followed by space at the start of your reply. 

Default STARTER_CHARACTER if no other character is specified = 🧩

This project uses a test-first, incremental workflow. Use this process for all new functionality and refactors.

always use JSpecify for nullablilty check, add `@NullMarked` to  all packages  to indicate that the remaining unannotated type usages are not nullable. Create package-info.java in every package, Avoid annotating every class individually

use `gradle` as the build tool



# Principles for writing automated tests

STARTER_CHARACTER for generating tests = 🔍

1. **Write tests before production code**
   - Define or extend tests that express the desired behaviour and edge cases.
   - Only then implement or change production code.

2. **Align on test cases first**
   - Discuss and agree test scenarios (including edge cases) before implementation.
   - Treat tests as the primary executable specification.

3. **Make tests fail meaningfully first**
   - Prefer failures where the code is implemented but *wrong* (e.g. incorrect return values), so the failing assertions describe behavioural gaps rather than "not implemented" errors.

4. **Work in small, focused steps**
   - Change one small, coherent piece of behaviour at a time.
   - Keep each test or code change narrowly scoped and immediately verifiable.
   - **NEVER create large commits** with many production classes at once. Each production class with logic should have its test committed together.

5. **Avoid magic literals in tests**
   - When a value is used repeatedly in a test suite, define it once as a `const` and reuse it.
   - This reduces duplication and keeps tests readable and maintainable.

6. **Avoid brittle implementation-detail expectations**
   - Prefer assertions about *observable behaviour* and final outputs.
   - Only assert on implementation details (like mock call counts) when the *behaviour* depends on them.

7. **Split tests by concern**
   - Place each logical group of tests in a separate file or nested class.
   - Use `@Nested` classes for logical grouping within a test file.

## Test Classification (Naming Convention)

This project follows the **Maven Surefire/Failsafe convention** for separating test types:

| Type | File Pattern | Speed | Dependencies | Gradle Command |
|------|--------------|-------|--------------|--------------|
| **Unit Tests** | `*Test.java` | Fast (<1s) | Mocked | `./gradlew test` |
| **Integration Tests** | `*IT.java` | Slow (10-30s) | Real (DB, network) | `./gradlew test --tests '*IT'` |

### Unit Tests (`*Test.java`)
- Use **mocks** for all external dependencies (database, network, etc.)
- Run in milliseconds without Docker or external services
- Test business logic in isolation
- Example: `IngestionServiceTest.java`

```java
// Unit test - uses mocks, runs fast
@DisplayName("IngestionService")
class IngestionServiceTest {
    @Test
    void ingestsBook() {
        VectorStore mockVectorStore = mock(VectorStore.class);
        // ... mock setup and assertions
    }
}
```

### Integration Tests (`*IT.java`)
- Use **real dependencies** (Testcontainers, network calls)
- Require Docker for database containers
- Test full integration with external systems
- Example: `IngestionServiceIT.java`

```java
// Integration test - uses real DB, runs slower
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class IngestionServiceIT {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(...);
    
    @Autowired
    private IngestionService ingestionService;
}
```

## Running Tests

```bash
# Unit tests only (fast, run frequently during development)
./gradlew test

# Integration tests only (slow, run before commit)
./gradlew test --tests '*IT'

# Run specific test class
./gradlew test --tests 'de.bas.bodo.genai.ingestion.IngestionServiceTest'

# Run all tests with coverage
./gradlew test
./gradlew jacocoTestReport
```

## Test Coverage

- Coverage is collected via **JaCoCo** (Gradle plugin)
- Both unit and integration tests contribute to coverage
- View coverage in VS Code/Cursor with **Coverage Gutters** extension
- Generate HTML report: `./gradlew jacocoTestReport`

### Coverage Requirements

| Metric | Minimum Threshold |
|--------|-------------------|
| Instruction coverage | **80%** |
| Branch coverage | **60%** |

### What MUST Be Tested

Every production class with **behaviour** (methods beyond getters/constructors) requires a dedicated test:

| Class Type | Test Required? | Notes |
|------------|----------------|-------|
| Services, Controllers | ✅ **Yes** | Always requires `*Test.java` |
| Spring `@Component` / `@EventListener` | ✅ **Yes** | Including lifecycle beans like `DataInitializer` |
| Utility classes | ✅ **Yes** | e.g., `TextChunker`, parsers, validators |
| Records with factory methods | ✅ **Yes** | e.g., `RagResponse.success()`, `IngestionResult.failure()` |
| Simple DTOs / records (data only) | ⚠️ Indirect | Covered via tests of classes that use them |
| Enums | ⚠️ Indirect | Covered via tests of classes that use them |

### What Does NOT Need a Dedicated Test

- Simple records with only a canonical constructor (e.g., `BookContent(int id, String title, String text)`)
- Enums with no logic (e.g., `GuardResult`)
- `package-info.java` files
- Main application class (`GenaiApplication.main()`) - Spring Boot convention

### Detecting Orphan Classes (No Test)

Before committing, verify every new production class has coverage:

```bash
# List production classes
find src/main -name "*.java" | grep -v package-info | sort > /tmp/prod.txt

# List test classes  
find src/test -name "*Test.java" -o -name "*IT.java" | sort > /tmp/tests.txt

# Manual review: each service/controller/component in prod should have a test
```

## Recommended Workflow for a New Feature

1. **Write failing unit test first** (`*Test.java`)
   - Use mocks for dependencies
   - Express expected behaviour
   - **Include ALL classes** that will have behaviour: services, controllers, components, event listeners

2. **Implement production code**
   - Make the unit test pass
   - Keep implementation minimal
   - **One production class = one test class** (for classes with logic)

3. **Add integration test if needed** (`*IT.java`)
   - Only for features requiring real external systems
   - Verify end-to-end behaviour

4. **Check coverage before committing**
   - Run `./coverage.sh` or use Coverage Gutters
   - Ensure new code is covered at **80%+**
   - **Review for orphan classes** - new production code without tests

## Common TDD Violations to Avoid

| Anti-Pattern | Problem | Solution |
|-------------|---------|----------|
| "Big Bang" commits | Many classes added without tests for all | Commit one feature at a time with its tests |
| "Glue code doesn't need tests" | Components like `DataInitializer` are skipped | All `@Component`, `@EventListener` classes need tests |
| "It's just a record" | Records with factory methods are untested | Test factory methods and any non-trivial logic |
| "Integration tests cover it" | Unit tests skipped because E2E exists | Unit tests are required; integration tests are supplementary |
| Redundant classes | Copy-pasted classes that are never used | Remove unused code; don't commit dead code |

## Pre-Commit Checklist

Before committing any code change, verify:

- [ ] Every new production class with behaviour has a corresponding `*Test.java`
- [ ] Tests were written **before** the production code (TDD)
- [ ] Coverage is at **80%+** for new code
- [ ] No orphan/unused classes exist
- [ ] Commit is small and focused (ideally one feature)

STARTER_CHARACTER for generating tests = 🔍


The overall software architecture should follow the Spring Modulith approach.

STARTER_CHARACTER for implementing production code to resolve red tests = ❗

## Principles for implementing production code 

1. **Test exists first (TDD)**
   - Make sure there is a **failing test before implementing any new feature**
   - See the test rules section above for details on how to write tests
   - This applies to **ALL production classes with behaviour**: services, controllers, components, event listeners, utilities

2. **Implement core logic**
   - Implement the Java code to satisfy the failing tests
   - Iterate until all relevant test suites pass
   - Keep implementation minimal - only what's needed to pass the test

3. **One class, one test**
   - Every production class with logic requires a corresponding `*Test.java`
   - **Never skip tests for "infrastructure" or "glue" code** (e.g., `@EventListener`, `@Component`)
   - Simple DTOs/records without methods can be covered indirectly

4. **Refine and tidy**
   - Extract repeated literals in tests into constants
   - Split or reorganize tests into focused files if they become large
   - Re-run all tests to confirm the full suite is green
   - **Check coverage** before considering the feature complete

5. **Java package structure**
   - Use Spring Modulith rules to structure packages
   - Prefer Java `record` over `class` for data types
   - Avoid creating redundant/duplicate classes

6. **Module boundaries and data ownership**
   - Each external resource (database, API, file system) should be **owned by exactly one module**
   - Other modules access shared resources **through the owning module's API**, never directly
   - Run `ModularityTests` to verify module dependencies are correct
   - Generate PlantUML diagrams to visualize and validate architecture

## What Requires a Dedicated Test

| Class Type | Example | Test Required? |
|------------|---------|----------------|
| Controllers | `ChatController` | ✅ Yes |
| Services | `IngestionService` | ✅ Yes |
| `@Component` / `@EventListener` | `DataInitializer` | ✅ **Yes** |
| Utility classes | `TextChunker` | ✅ Yes |
| Records with factory methods | `RagResponse.success()` | ✅ Yes |
| Simple records (data only) | `BookContent` | ⚠️ Indirect |

## Common Pitfalls to Avoid

| Mistake | Why It's Wrong | Correct Approach |
|---------|----------------|------------------|
| "This is just glue code" | `DataInitializer` has logic (counting, logging) | Write a unit test |
| "Integration tests cover it" | Unit isolation is lost | Unit tests first, then integration |
| "I'll add tests later" | Tests never get added | **TDD: test first, always** |
| "One big commit with everything" | Coverage gaps are hidden | Small, focused commits |
| Copying similar classes | Creates dead/redundant code | Reuse existing classes |
| Multiple modules accessing same DB | Tight coupling, unclear ownership | **One module owns the resource** |
| Injecting infrastructure directly | e.g., `VectorStore` in multiple modules | Inject the owning service instead |

## Commit Guidelines

- **Each commit should include production code AND its tests**
- Never commit production code without corresponding test coverage
- Keep commits small and focused on one feature/fix
- Verify coverage is **≥80%** before committing

## Spring Modulith Architecture Rules

### Module Data Ownership

Each shared resource (database, external API, file system) must be **owned by exactly one module**:

| Resource | Owning Module | Exposed API |
|----------|---------------|-------------|
| VectorStore (PostgreSQL/pgvector) | `retrieval` | `addDocuments()`, `findStoredBookIds()`, `retrieve()` |
| External APIs (e.g., OpenAI) | `generation` | `answer()` |
| Project Gutenberg downloads | `ingestion` | (internal use only) |

### Cross-Module Access Pattern

```
❌ WRONG: Multiple modules inject infrastructure directly
┌──────────────┐     ┌──────────────┐
│  Ingestion   │────▶│ VectorStore  │◀────│  Retrieval  │
└──────────────┘     └──────────────┘     └─────────────┘

✅ CORRECT: One module owns infrastructure, exposes API
┌──────────────┐     ┌──────────────────────┐
│  Ingestion   │────▶│     Retrieval        │
└──────────────┘     │  (owns VectorStore)  │
                     │  - addDocuments()    │
                     │  - retrieve()        │
                     └──────────────────────┘
```

### Verification Commands

```bash
# Verify module boundaries are respected
./gradlew test --tests 'de.bas.bodo.genai.ModularityTests'

# Generate architecture diagrams
# (diagrams saved to docs/modulith/*.puml)
```

### Adding a New Module

1. Create package under `de.bas.bodo.genai.<module_name>`
2. Add `package-info.java` with `@NullMarked`
3. If the module needs a shared resource:
   - Check if another module already owns it → use that module's API
   - If no owner exists → this module becomes the owner
4. Run `ModularityTests` to verify dependencies
5. Update PlantUML diagrams and commit them

STARTER_CHARACTER for implementing production code to resolve red tests = ❗
