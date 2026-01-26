# Agent Company Policy

## Mission
Generate high-quality, maintainable unit tests that verify behavior, not implementation.

## Testing Framework
- **Test Framework:** JUnit 5
- **Assertions:** AssertJ
- **Mocking:** Mockito

## Naming Conventions
- Test classes: `{ClassName}Test.java`
- Test methods: `should_{expectedBehavior}_when_{condition}`
- Examples:
  - `should_throwException_when_customerIdIsNull`
  - `should_returnTotal_when_itemsAreValid`
  - `should_cancelOrder_when_orderIsNotShipped`

## Test Structure
Use Arrange/Act/Assert (AAA) pattern with clear visual separation:
```java
@Test
void should_doSomething_when_someCondition() {
    // Arrange
    var input = createTestInput();
    when(mockDependency.method()).thenReturn(expectedValue);

    // Act
    var result = serviceUnderTest.methodToTest(input);

    // Assert
    assertThat(result).isEqualTo(expectedValue);
}
```

## Coverage Requirements
Each public method requires tests for:
1. **Happy path** — normal successful execution
2. **Edge cases** — null inputs, empty collections, boundary values
3. **Exception cases** — all explicitly thrown exceptions
4. **Business rules** — any conditional logic

## Quality Standards
- Tests must be independent (no shared mutable state)
- One logical assertion per test (multiple AssertJ chained assertions are fine)
- Use descriptive variable names (`validCustomerId`, `emptyItemList`)
- Mock all external dependencies
- Never test private methods directly
- No hardcoded magic values without explanation

## AssertJ Patterns
Prefer fluent AssertJ assertions:
```java
// Good
assertThat(result).isNotNull();
assertThat(result.getItems()).hasSize(3).contains(expectedItem);
assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("100.00"));

// For exceptions
assertThatThrownBy(() -> service.method(invalidInput))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("cannot be empty");
```

## File Organization
- One test class per source class
- Test class lives in `src/test/java` mirroring source package structure
- Use `@DisplayName` for complex test scenarios (optional)

## What We Do NOT Do
- No integration tests (unit tests only)
- No testing of third-party library behavior
- No overly complex test setup (if setup exceeds 10 lines, reconsider design)
- No testing getters/setters unless they contain logic