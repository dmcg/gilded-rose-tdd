# gilded-rose-tdd

A TDD implementation of the Gilded Rose stock control system

You can see the development of this code on [YouTube](https://youtube.com/playlist?list=PL1ssMPpyqocg2D_8mgIbcnQGxCPI2_fpA)

## Setup

Requires Java 21 and Docker

### Initialise the test database

```bash
./gradlew flywayMigrate generateJooq
```

### Run (most of) the Tests

```bash
./gradlew clean test
```

### Initialise the local database

```bash
db/migrate-dev.sh
```

### Populate the database with test data

Run src/test/java/populate-main.kt

### Run the app

Run src/main/java/main.kt


### 47 refactorings in 45 minutes references

- [47 refactorings in 45 minutes](https://www.youtube.com/watch?v=XJ9fq-PYCWk&t=26390s) at KotlinConf 2025
- [Refactoring to Expressive Kotlin](https://www.youtube.com/watch?v=p5WylVjtzBQ) at KotlinConf 2024
- [Pairing with Duncan](https://www.youtube.com/@PairingWithDuncan) YouTube channel
- [Tidy First?](https://www.oreilly.com/library/view/tidy-first/9781098151232) book by Kent Beck
- [Quick Fix](https://github.com/dkandalov/quick-fix) plugin for IntelliJ
