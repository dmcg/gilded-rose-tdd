# Refactoring Steps

## Introduce the app
- Stock control for Gilded Rose Inc
- It's currently being migrated from the machine under Alison's desk to The Cloud
- And hence is using both a flat file and a database for storage
- Show the app (it looks great when we use the proper CSS file, but that is under NDA)
- Run the tests

## Introduce the code
- We have some places where we haven't yet gotten around to applying our new-found expressive Kotlin knowledge
- We have a little list, so let's go on a tour and see what we can make better
-
## Scope Functions

### Apply
#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)
- Refactor `hikariDataSourceFor()` in  to use `apply`

### Run
#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)
- In hikariDataSourceFor if we want to see the jdbcUrl we can introduce a var in a run

### Also
#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)
- Refactor `::DbConfig` to println url using `also(::println)`
- using `printed()` (almost like postfix completion)
- Refactor `dslContextFor` to use `also`
- Point out that `apply` would also do

#### [DualItems](src/main/java/com/gildedrose/persistence/DualItems.kt)
- MAYBE Refactor `save` and `load` to use `also`

### With
#### [DbItems](src/main/java/com/gildedrose/persistence/DbItems.kt)
- Refactor `DSLContext.save` and `DSLContext.load` to use `with`
- load can now be a single expression
- with is like an import from a variable
- we could use multiple `with` in save for item - bad idea generally

### Let
#### [Quality](src/main/java/com/gildedrose/domain/Quality.kt)
- Introduce .let with if inside let
- rename wrapper to it
- and then ?.let
- talk about ?:
- undo and show that IJ will do it for us (replace with safe access)
-
#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)
- Use let to convert dslContextFor into a nice chain

## Extension Functions
#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)
- Extract extension fun DataSource.toDslContext from DSL.using(it, SQLDialect.POSTGRES)
- Now the let can go
- Now hikariDataSourceFor can be an extension and we have a straight through chain
- but we have an issue with this@toHikariDataSource - .also, or back out, - try inline - things go badly wrong, fix with .also
- Make dslContextFor an extension as well and look at where it is called

### Coupling/cohesion
#### [configuring.kt](src/main/java/com/gildedrose/config/configuring.kt)
- Note that we couldn't move DataSource.toDslContext to a method, as we don't own DataSource
- We could move DbConfig.toDslContext to a method. But if we did we couple the DbConfig to Hikari and DSLContext. So we couldn't remove it from this module.
- DbConfig is coupled to Environment by the constructor - fix that with a top-level function DbConfig
- See that that hasn't changed any of the callers, although it is a breaking change


### Tiny types
#### [Item and PricedItem](src/main/java/com/gildedrose/domain/Item.kt)
- Note that both id and name are effectively String, despite the typealias
- This can cause issues like the ones we had with the wrong variable binding
- and Item has preconditions that are not communicated to its clients
- add a typeAlias for ItemName
- and use it in the data flow paths to name
- add a constructor fun ItemName(s: String) = s and call it in the places where we map from strings
- Now create a value class ItemName
- we have add some .values to compile
- but when we're done we can move the require from Item and PricedItem into ItemName
- and then create a companion object ctor that return Item?
- and fix up the callers
- so that we have moved the check to the edge


#### [Quality](src/main/java/com/gildedrose/domain/Quality.kt)
- (Quality.invoke() could be standalone function but is it more expressive? 🤔)

### Actions

### DSLs
