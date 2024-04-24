# Refactoring Steps

## Scope Functions

### Apply
- Refactor `hikariDataSourceFor()` in  to use `apply` (in [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt))
-

### Also
#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)
- Refactor `toDslContext` to use `also`
- Refactor `::DbConfig` to us `also` to print the database url
  printed()? (almost postfix completion)

#### [DualItems](src/main/java/com/gildedrose/persistence/DualItems.kt)
- Refactor `save` and `load` to use `also`

### Run
#### [AddItemHttpTests](src/test/java/com/gildedrose/AddItemHttpTests.kt)
- Refactor `postFormToAddItemsRoute` to use `run`

### With
#### [DualItems](src/main/java/com/gildedrose/persistence/DualItems.kt)
- Refactor `DSLContext.save` and `DSLContext.load` to use `with`

### Coupling/cohesion
#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)

### Tiny types
#### [Item and PricedItem](src/main/java/com/gildedrose/domain/Item.kt) using NonBlankString
#### [Quality](src/main/java/com/gildedrose/domain/Quality.kt)

TODO remove `BlankName`; remove error after `name = this[ITEMS.NAME]`
