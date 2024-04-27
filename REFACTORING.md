# Refactoring Steps

## Scope Functions

### Apply
- Refactor `hikariDataSourceFor()` in  to use `apply` (in [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt))

### Also
#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)
- Refactor `::DbConfig` to println url using `also(::println)`
- using `printed()` (almost like postfix completion)
- Refactor `toDslContext` to use `also`

#### [DualItems](src/main/java/com/gildedrose/persistence/DualItems.kt)
- Refactor `save` and `load` to use `also`

### Run
#### [AddItemHttpTests](src/test/java/com/gildedrose/AddItemHttpTests.kt)
- Refactor `postFormToAddItemsRoute` to use `run`
  (note that `.header("HX-Request", null)` is basically `HX-Request: ` with empty value)

### With
#### [DbItems](src/main/java/com/gildedrose/persistence/DbItems.kt)
- Refactor `DSLContext.save` and `DSLContext.load` to use `with`
- multiple `with`
- extract `with` into extension function

### Coupling/cohesion
#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)

### Tiny types
#### [Item and PricedItem](src/main/java/com/gildedrose/domain/Item.kt) using NonBlankString
- (NonBlankString can't be a standalone function because it uses private constructor with the same signature)

#### [Quality](src/main/java/com/gildedrose/domain/Quality.kt)
- (Quality.invoke() could be standalone function but is it more expressive? 🤔)
