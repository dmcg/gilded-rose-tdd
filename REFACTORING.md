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

## [DbConfig.kt](src/main/java/com/gildedrose/config/DbConfig.kt)
- `fun dslContext` could be communicating better, rename to `toDslContext`
- Now it's doing several things, the first is creating a DataSource
- Extract a `hikariDataSourceFor`
- Is there any reason for it DbConfig to be coupled to HikariDataSource? No
- Move `hikariDataSourceFor` it to the top level
- Now we would like to use `apply`

## Scope Functions

### Apply
#### [DbConfig.kt](src/main/java/com/gildedrose/config/DbConfig.kt)
- Refactor `hikariDataSourceFor()` to use `apply` via `val dataSource = this`
- Maybe apply should have been called with?


### Also
#### [DbConfig.kt](src/main/java/com/gildedrose/config/DbConfig.kt)
- Show our standard refactoring on `dslContextFor` `dataSource.validate()`
- Intention on second `dataSource` will also do it


### Run
#### [DbConfig.kt](src/main/java/com/gildedrose/config/DbConfig.kt)
- Surround `dbConfig.jdbcUrl.toString()` with run
- Add in a println("here")
- run converts an expression into a scope
- show also instead
- and apply
- and then printed

### With
#### [DbItems](src/main/java/com/gildedrose/persistence/DbItems.kt)
- Refactor `DSLContext.save` to use `with`
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
#### [DbConfig.kt](src/main/java/com/gildedrose/config/DbConfig.kt)
- Use let to convert dslContextFor into a nice chain

## Extension Functions
#### [DbConfig.kt](src/main/java/com/gildedrose/config/DbConfig.kt)
- Extract extension fun DataSource.toDslContext from DSL.using(it, SQLDialect.POSTGRES)
- Now the let can go
- Now hikariDataSourceFor can be an extension and we have a straight through chain
- but we have an issue with this@toHikariDataSource - revert and show that the for works at the beginning of a chain
- Make dslContextFor an extension as well and look at where it is called

### Coupling/cohesion
#### [DbConfig.kt](src/main/java/com/gildedrose/config/DbConfig.kt)
- Note that we couldn't move DataSource.toDslContext to a method, as we don't own DataSource
- We could move DbConfig.toDslContext to a method. But if we did we couple the DbConfig to Hikari and DSLContext. So we couldn't remove it from this module.
- DbConfig is coupled to Environment by the constructor - fix that with a top-level function DbConfig
- See that that hasn't changed any of the callers, although it is a breaking change


### Tiny types
#### [Item and PricedItem](src/main/java/com/gildedrose/domain/Item.kt)
- Note that both id and name are effectively String, despite the typealias
- This can cause issues like the ones we had with the wrong variable binding
- and Item has preconditions that are not communicated to its clients
- We're going to expand-contract refactor
- Add a data class ItemName, with init require
- Change the type of the two names to ItemName - the callers and references all break.
- Add an Item constructor taking string
- rename the val to _name locally (and PricedItem)
- add a computed val name (and PricedItem)
- add a .copy(name: String)
- That required no client changes - check in
- Inline the `val name get`s
- rename _names to name
- inline the .copy
- Make the Item secondary constructor a factory
- and inline it
- That fixed up our clients - check in
- Move the require isNotBlank into ItemName init
- Move ItemName ctor to companion
- Add ItemName? companion - can't compile
- add dummy: Boolean = false to the ItemName? one
- and make the ItemName call it with ?: error("Name must not be blank")
- Now inline the ItemName one
- and delete the dummy
- Now go through the callers and do the right things
- Finally we can look at the name.value and elide some of those - note typeFor where CharSequence


### Actions

### DSLs
