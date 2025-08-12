Project: gilded-rose-tdd — Advanced Development Guidelines

Audience: Experienced Kotlin/JVM developers contributing to this codebase.

1. Build and Configuration
- Toolchains and prerequisites
  - Java: Toolchain is pinned to Java 21 via the shared kotlin-conventions Gradle plugin (buildSrc). Ensure JDK 21 is available to Gradle (Gradle toolchains will provision it if needed).
  - Gradle: Wrapper is provided. Always use ./gradlew.
  - Docker: Required for local and test Postgres instances and for Flyway/JOOQ generation tasks that expect running DBs.

- Multi-module layout
  - Modules: foundation, core, database, app, test-app, experiments.
  - Shared Gradle plugin: buildSrc/src/main/kotlin/kotlin-conventions.gradle.kts configures Kotlin/JUnit 5, Java toolchain, and Kotlin compiler args (-Xcontext-parameters).

- Databases and migrations
  - Docker compose spins up two Postgres containers:
    - local-database (port 5432): for manual app usage and dev data.
    - test-database (port 5433): used by database code generation (JOOQ) and can be used for isolated testing.
  - Start both:
    - scripts/start-db.sh (uses scripts/docker-compose.yml)
  - Migrations (Flyway):
    - Dev DB (5432): ./scripts/migrate-dev.sh (runs flywayMigrateDev against local-database).
    - Codegen/Test DB (5433): flywayMigrate task is wired to jdbc:postgresql://localhost:5433/gilded-rose and is depended on by generateJooq.
  - JOOQ code generation (database module):
    - Preconditions: test-database must be up on port 5433 and migrated.
    - Tasks: ./gradlew :database:generateJooq (depends on :database:flywayMigrate). If schemas/migrations change, re-run generateJooq.

- Typical build flows
  - Fast check of core/app without DB: ./gradlew :core:test :app:test
  - Full repo build (may require DB up for database module codegen): ./gradlew clean build
  - If database tasks fail due to missing DB, either start docker via scripts/start-db.sh or omit database module in the invocation.

2. Testing
- Test framework and parallelism
  - JUnit 5 platform across modules; kotlin("test") is used for assertions. Tests run under JUnit Platform with parallel class execution tuned in app/src/test/resources/junit-platform.properties (32 threads, same_thread within class, concurrent between classes).

- Running tests
  - Whole repository: ./gradlew clean test
  - Per module:
    - Core: ./gradlew :core:test
    - App: ./gradlew :app:test
    - Database (requires DB 5433 up): ./gradlew :database:test
    - Test-app (Playwright): ./gradlew :test-app:test
  - Single class or pattern:
    - ./gradlew :core:test --tests com.gildedrose.updating.StandardUpdatingTests
    - Wildcard patterns are supported by JUnit Platform:
      ./gradlew :app:test --tests "com.gildedrose.http.*"
  - Single test method:
    - ./gradlew :core:test --tests "com.gildedrose.updating.StandardUpdatingTests.items decrease in quality one per day"

- Adding tests
  - Locations:
    - Unit and integration tests: <module>/src/test/kotlin (Kotlin) or <module>/src/test/java (Java).
    - Shared test fixtures: core and app expose testFixtures. Reuse fixtures from core where possible (e.g., core/src/testFixtures/kotlin/com/gildedrose/testing and ItemsContract).
  - Conventions:
    - Prefer Kotlin + JUnit 5 (kotlin.test.Test). Keep test classes/package under com.gildedrose.* to align with existing structure.
    - For approval tests in app (http4k-approval), commit the approved files under app/src/test/resources.
    - For browser (Playwright) tests in test-app: tests spin up an in-process http4k server for routes; ensure browsers are available (Playwright downloads on first run, requires internet). Flip showBrowserTests in test sources to view the browser if needed.
  - Database-related tests:
    - database/build.gradle.kts points JOOQ and Flyway at the test-database on 5433. Ensure scripts/start-db.sh is running before running database tests or generateJooq.

3. Additional Development Information
- Code style and structure
  - Domain-first design: core module contains the domain model (Item, StockList, ItemType, etc.) and updating logic. Keep domain pure and free from infrastructure concerns.
  - app module wires http4k routes, rendering, pricing integration, and analytics. Prefer injecting dependencies and keeping http handlers side-effect-light for testability.
  - database module encapsulates persistence with JOOQ and Flyway. Do not leak JOOQ types outside; map to domain models at boundaries.
  - foundation hosts generic infra (analytics helpers, parallel mapping, retrying) and should remain reusable.

- Useful testing patterns and utilities
  - Contracts: core/src/testFixtures/kotlin/com/gildedrose/domain/ItemsContract.kt provides a contract test for any Items implementation. Implementations (e.g., in-memory, DB-backed) should satisfy this contract.
  - Test timing utilities: app/src/main/java/com/gildedrose/testing/TestTiming.kt and app/src/test/java/com/gildedrose/testing handle timing and ordering; JUnit class ordering is enabled via junit-platform.properties.
  - Approval tests: app/src/test/resources contains .approved files used by http4k approval tests. Keep formatting stable and normalize HTML when comparing (see test helpers).

- Running the application
  - Populate dev DB (optional): run app/src/test/java/populate-main.kt to insert sample data after running scripts/migrate-dev.sh.
  - Start app for manual testing: run app/src/main/java/main.kt (http4k Undertow server), or use app/src/main/java/com/gildedrose/app.kt entrypoints.

- Common pitfalls
  - Database availability: database:generateJooq and database:test expect a Postgres at localhost:5433. Use scripts/start-db.sh to start both DBs; you can run tests for core/app without DB.
  - Parallel tests: app tests run classes concurrently. If you add shared mutable state in tests or fixtures, annotate appropriately or avoid shared state.
  - Playwright: first run may download browsers; ensure network access. If headless flakiness occurs, run with showBrowserTests=true and slowMo for debugging.

- Handy commands
  - Start DBs: scripts/start-db.sh
  - Migrate dev DB: ./scripts/migrate-dev.sh
  - Generate JOOQ: ./gradlew :database:generateJooq
  - Run subset of tests: ./gradlew :app:test --tests "com.gildedrose.http.ResponseErrorsTests"
  - Full clean test: ./gradlew clean test

Maintenance note
- These guidelines assume the current Gradle tasks and ports as configured in the repository at the time of writing. If ports or tasks change in database/build.gradle.kts or scripts/docker-compose.yml, update this document accordingly.
