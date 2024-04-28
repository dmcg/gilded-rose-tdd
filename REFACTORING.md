# Refactoring Steps

## Scope Functions

### Apply
- Refactor `hikariDataSourceFor()` in  to use `apply` (in [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt))
-

### Also
#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)
- Refactor `::DbConfig` to println url using `also(::println)`
- using `printed()` (almost like postfix completion)
- Refactor `dslContextFor` to use `also`
- Point out that `apply` would also do

#### [DualItems](src/main/java/com/gildedrose/persistence/DualItems.kt)
- Refactor `save` and `load` to use `also`

### Run
#### [app.kt](src/main/java/com/gildedrose/app.kt)
- itemsFor could use run to make a single expression
- then we can inline that into the ctor
- talk about receiver
- probably shouldn't do this here, but useful when we want a temporary single expression

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
#### [Quality](src/main/java/com/gildedrose/domain/Quality.kt)
- Introduce .let with if inside let
- and then ?.let
#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)
- Use let to convert dslContextFor into a nice chain

## Extension Functions
#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)
- Extract extension fun DataSource.toDslContext from DSL.using(it, SQLDialect.POSTGRES)
- Now the let can go
- Now hikariDataSourceFor can be an extension and we have a straight through chain
- but we have an issue with this@toHikariDataSource - .also, or back out, or inline
- Make dslContextFor an extension as well and look at where it is called

### Coupling/cohesion
#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)
- Note that we couldn't move DataSource.toDslContext to a method, as we don't own DataSource
- We could move DbConfig.toDslContext to a method. But if we did we couple the DbConfig to Hikari and DSLContext. So we couldn't remove it from this module.
- DbConfig is coupled to Environment by the constructor - fix that with a top-level function DbConfig
- See that that hasn't changed any of the callers, although it is a breaking change


### Tiny types
#### [Item and PricedItem](src/main/java/com/gildedrose/domain/Item.kt) using NonBlankString
- (NonBlankString can't be a standalone function because it uses private constructor with the same signature)

#### [Quality](src/main/java/com/gildedrose/domain/Quality.kt)
- (Quality.invoke() could be standalone function but is it more expressive? 🤔)
