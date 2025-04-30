# gilded-rose-tdd

A TDD implementation of the Gilded Rose stock control system

You can see the development of this code on [YouTube](https://youtube.com/playlist?list=PL1ssMPpyqocg2D_8mgIbcnQGxCPI2_fpA)

## Setup

Requires Java 21 and Docker

### Start Postgres in Docker

```bash
scripts/start-db.sh
```

### Run (most of) the Tests

```bash
./gradlew clean test
```

### Initialise the local database

```bash
scripts/migrate-dev.sh
```

### Populate the database with test data

Run app/src/test/java/populate-main.kt

### Run the app

Run app/src/main/java/main.kt
