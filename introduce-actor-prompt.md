# Refactoring Acceptance Tests to Use the Actor Pattern

## Goal
Refactor acceptance test contracts to use a consistent Actor pattern, consolidating duplicate implementations and improving code maintainability.

## Background
In test-driven development, acceptance tests often follow similar patterns across different features. Using the Actor pattern provides a consistent way to interact with the system under test, whether through direct API calls, HTTP requests, or UI interactions. This refactoring aims to:

1. Convert acceptance test contracts to use a unified Actor interface
2. Consolidate duplicate actor implementations
3. Move specialized code to appropriate utility files
4. Ensure consistent patterns across the codebase

## Step-by-Step Refactoring Process

### 1. Identify the Acceptance Test Contracts
- Look for abstract classes that define acceptance tests
- Identify how these tests currently interact with the system (direct calls, HTTP, UI)
- Note any duplicated patterns across different test files

### 2. Define a Common Actor Interface
- Create an abstract Actor class with methods for all required interactions
- For example:
  ```kotlin
  abstract class Actor {
      abstract fun performAction(fixture: Fixture, params: Params)
      abstract fun verifyResult(fixture: Fixture, expected: Expected)
  }
  ```

### 3. Implement Concrete Actor Classes
- Create implementations for each interaction method (Direct, HTTP, UI)
- Move these implementations to separate files if they're substantial
- Ensure each actor implements all required methods from the interface

### 4. Update Acceptance Test Contracts
- Modify the contracts to accept an Actor in their constructor
- Replace direct interaction code with calls to the Actor methods
- For example:
  ```kotlin
  abstract class FeatureAcceptanceContract(
      val actor: Actor
  ) {
      @Test
      fun `test feature`() {
          Given(fixture)
          .When {
              actor.performAction(this, params)
          }
          .Then {
              checkResult(expected)
          }
      }
  }
  ```

### 5. Update Test Implementations
- Modify test classes to use the appropriate Actor implementation
- For example:
  ```kotlin
  class FeatureDirectTests : FeatureAcceptanceContract(
      actor = DirectActor()
  )

  class FeatureHttpTests : FeatureAcceptanceContract(
      actor = HttpActor()
  )
  ```

### 6. Extract Utility Functions
- Move common utility functions to appropriate utility files
- Update references to use these utility functions
- Ensure consistent naming and parameter patterns

### 7. Remove Duplicate Code
- Identify and remove any specialized actor classes that are now redundant
- Ensure all tests still work with the consolidated actors

### 8. Verify the Refactoring
- Run all tests to ensure they still pass
- Build the project to check for compilation errors
- Review the code for consistency and clarity

## Example from the Codebase

### Before Refactoring:
```kotlin
abstract class AddItemAcceptanceContract(
    private val add: (Fixture, Item) -> Unit,
) {
    @Test
    fun `add item`() {
        Given(fixture)
        .When {
            add(this, newItem)
        }
        .Then {
            checkResult(expected)
        }
    }
}

class AddItemDirectlyTests : AddItemAcceptanceContract(
    add = { fixture: Fixture, item: Item -> fixture.app.addItem(item) }
)
```

### After Refactoring:
```kotlin
abstract class AddItemAcceptanceContract(
    val actor: Actor
) {
    @Test
    fun `add item`() {
        Given(fixture)
        .When {
            actor.adds(newItem)
        }
        .Then {
            checkResult(expected)
        }
    }
}

class AddItemDirectlyTests : AddItemAcceptanceContract(
    actor = DirectActor()
)

class DirectActor : Actor() {
    override fun add(fixture: Fixture, item: Item) {
        fixture.app.addItem(item)
    }

    override fun delete(fixture: Fixture, items: Set<Item>) {
        // Implementation for delete
    }
}
```

## Common Challenges and Solutions

### Challenge: Different Actor Implementations Need Different Utility Functions
**Solution:** Create utility files specific to each actor type (e.g., HttpTestUtils.kt) and move relevant functions there.

### Challenge: Some Tests Need Special Handling
**Solution:** Override specific test methods in the concrete test classes when needed, or add optional parameters to the Actor methods.

### Challenge: Maintaining Backward Compatibility
**Solution:** Implement the refactoring incrementally, ensuring each step maintains working tests before proceeding to the next.

### Challenge: Duplicate Code Across Actor Implementations
**Solution:** Extract common code to base classes or utility functions that can be shared across actor implementations.

## Benefits of This Refactoring

1. **Improved Maintainability:** Changes to test interactions only need to be made in one place
2. **Better Readability:** Consistent patterns make the code easier to understand
3. **Easier Extension:** Adding new interaction methods only requires implementing them in the actor classes
4. **Reduced Duplication:** Common code is consolidated and reused
5. **Clearer Separation of Concerns:** Test logic is separated from interaction details

By following this refactoring pattern, you can significantly improve the structure and maintainability of your acceptance tests while ensuring they remain effective at validating system behavior.
