# Refactoring Steps

## Scope Functions

### Apply

#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)

Refactor `hikariDataSourceFor` in  to use `apply`

### Also

#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)

Refactor `toDslContext` to use `also`

Refactor `::DbConfig` to us `also` to print the database url

#### [DualItems](src/main/java/com/gildedrose/persistence/DualItems.kt)

Refactor `save` and `load` to use `also`

### Run

#### [AddItemHttpTests](src/test/java/com/gildedrose/AddItemHttpTests.kt)

Refactor `postFormToAddItemsRoute` to use `run`

### With

#### [DualItems](src/main/java/com/gildedrose/persistence/DualItems.kt)

Refactor `DSLContext.save` and `DSLContext.save` to use `with`
