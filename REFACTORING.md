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
TODO - this isn't a good example. Just find a place that needs a little local scope
- Refactor `postFormToAddItemsRoute` to use `run`
  (note that `.header("HX-Request", null)` is basically `HX-Request: ` with empty value)

### With
#### [DbItems](src/main/java/com/gildedrose/persistence/DbItems.kt)
- Refactor `DSLContext.save` and `DSLContext.load` to use `with`
- multiple `with`
- extract `with` into extension function

### Let
- Start with a simple example of converting a local variable into an expression
- Quality invoke (decide if that is invoke or a top level function)
- Add an example of ?.let
- Segue into chaining

## Extension Functions
#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)
- Make some things into extensions
- Can

### Coupling/cohesion
#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)

### Tiny types
#### [Item and PricedItem](src/main/java/com/gildedrose/domain/Item.kt) using NonBlankString
- (NonBlankString can't be a standalone function because it uses private constructor with the same signature)

#### [Quality](src/main/java/com/gildedrose/domain/Quality.kt)
- (Quality.invoke() could be standalone function but is it more expressive? 🤔)