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


## 47 refactorings in 45 minutes references

- [47 refactorings in 45 minutes](https://www.youtube.com/watch?v=XJ9fq-PYCWk&t=26390s) at KotlinConf 2025
- [Refactoring to Expressive Kotlin](https://www.youtube.com/watch?v=p5WylVjtzBQ) at KotlinConf 2024
- [Pairing with Duncan](https://www.youtube.com/@PairingWithDuncan) YouTube channel
- [Tidy First?](https://www.oreilly.com/library/view/tidy-first/9781098151232) book by Kent Beck
- [Quick Fix](https://github.com/dkandalov/quick-fix) plugin for IntelliJ
- [Big Test Progress Bar](https://gist.github.com/dkandalov/765232732a0ef7d2937861fcb035b804) plugin for IntelliJ
- [Keyboard Shortcuts Presenter](https://gist.github.com/dkandalov/54839566c1de9e6012c93bcb87309306) mini-plugin for IntelliJ
- [Refactorings counter](https://gist.github.com/dkandalov/cd7e987ac171d18d79637f6d6c66bea0) mini-plugin for IntelliJ
- [Convert secondary constructor to top-level function](https://gist.github.com/dkandalov/176103506770b8b12a9b67e006dd35bc) mini-plugin for IntelliJ