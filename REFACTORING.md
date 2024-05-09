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

## TODOs
- use `Call Hierarchy` at some point
- use `Data Flow to here` at some point
- mention different types of `{` at some point
- use `Jump to Navigation Bar`
- use `Quick Definition`

## [DbConfig.kt](src/main/java/com/gildedrose/config/DbConfig.kt)
- `fun dslContext` could be communicating better, rename to `toDslContext`
- Now `hikariDataSource` could be `hikariDataSourceFor`
- Now it's doing several things, the first is creating a DataSource
- Refactor `hikariDataSourceFor()` to use `apply` via `val dataSource = this`
- `apply` is like a conjunction in English - `with`. It joins two clauses.
- Now that we've created our dataSource, we also want to validate it
- introduce `.also`
- Now we've changed from 'do this' to 'I want a'
- Inline and make a single expresssion - again making more 'I want a'
- Should we include the return type? Again an opportunity to express something. In this case the single expression is complicated enough to include it.
- Now `toDslContext` is also saying 'do this' - assign to a var, use it - Two statements.
- We can remove one by inlining `dataSource`
- But now the ordering is wrong, we have to know that `hikariDataSourceFor` is evaluated first
- Introduce `let` - execution now follows reading order
- Extract dslContextFor(DataSource)
- This isn't using anything from DbConfig, so move it to the top level
- Make an extension
- Rename to `toDslContext`
- Now the `.let` is redundant - both extensions and let pipe
- Inline `hikariDataSourceFor` - note that it breaks - even IJ gets confused with too many this's
- Fix with this@DbConfig, but at this point we are talking to the compiler, not a human.
- Undo
- Now `toDslContext` makes DbConfig depend on Hikari and jOOQ
- We want the name, but not the coupling, so convert to extension
- We can do the same with the environment ctor, `Environment.toDbContext()`

### With
#### [DbItems](src/main/java/com/gildedrose/persistence/DbItems.kt)
- Refactor `DSLContext.save` to use `with`
- with is like an import from a variable
- we could use multiple `with` in save for item - bad idea generally


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

### DSLs
