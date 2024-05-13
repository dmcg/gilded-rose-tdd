# gilded-rose-tdd

A TDD implementation of the Gilded Rose stock control system

You can see the development of this code on [YouTube](https://youtube.com/playlist?list=PL1ssMPpyqocg2D_8mgIbcnQGxCPI2_fpA)

## Setup

Requires Java 21

### Initialise the test and dev databases

```bash
./gradlew initDbs
```

### Build and test

```bash
./gradlew clean test
```

### Populate the database with test data

Run src/test/java/populate-main.kt

### Run the app

Run src/main/java/main.kt
