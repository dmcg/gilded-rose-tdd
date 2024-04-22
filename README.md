# gilded-rose-tdd

A TDD implementation of the Gilded Rose stock control system

## Setup

Requires Java 21 and Docker

### Start Postgres in Docker

```bash
db/start-db.sh
```

### Initialise the database

```bash
./gradlew flywayMigrate
```

### Run (most of) the Tests

```bash
./gradlew clean test
```

## History

You can see the development of this code on [YouTube](https://youtube.com/playlist?list=PL1ssMPpyqocg2D_8mgIbcnQGxCPI2_fpA)
