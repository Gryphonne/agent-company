# Testing Policy

## Mission

Generate high-quality, maintainable unit tests that verify **behavior, not implementation**. A good test answers the question: "Given this input and state, does the class produce the correct outcome?" It does not answer: "Does the class call these methods in this order?"

## Technology Stack

| Concern     | Tool                       |
|-------------|----------------------------|
| Framework   | JUnit 5 (Jupiter)          |
| Assertions  | AssertJ                    |
| Mocking     | Mockito + MockitoExtension |
| Build       | Maven (Surefire plugin)    |

Do not introduce additional testing libraries unless explicitly instructed.

## Naming Conventions

### Test classes

`{ClassName}Test.java` — placed in `src/test/java`, mirroring the source package structure exactly.

### Test methods

Pattern: `should_{expectedBehavior}_when_{condition}`

The method name must read as a sentence describing the behavior:

```
should_throwException_when_customerIdIsNull
should_returnTotal_when_allItemsAreValid
should_cancelOrder_when_orderIsNotYetShipped
should_returnEmptyList_when_repositoryHasNoRecords
should_mapAllFields_when_sourceIsComplete
```

Avoid vague names like `should_work_when_valid` or `should_handleEdgeCase`. Be specific about *what* is expected and *what* triggers it.

## Test Structure

Every test follows the **Arrange / Act / Assert** pattern with blank line separation:

```java
@Test
void should_doSomething_when_someCondition() {
    // Arrange
    var input = createValidInput();
    when(mockDependency.fetch(input.getId())).thenReturn(expectedEntity);

    // Act
    var result = serviceUnderTest.process(input);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(Status.COMPLETE);
}
```

Rules:

- The `// Arrange`, `// Act`, `// Assert` comments are **required** — they enforce structure and readability
- **Act** should be a single statement wherever possible
- **Assert** should verify the outcome, not the journey — avoid excessive `verify()` calls unless the side effect IS the behavior being tested

## Class Setup Pattern

Always use explicit construction, never `@InjectMocks`:

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentGateway paymentGateway;

    private OrderService serviceUnderTest;

    @BeforeEach
    void setUp() {
        serviceUnderTest = new OrderService(orderRepository, paymentGateway);
    }
}
```

This makes the dependency list explicit and the test breaks visibly if the constructor changes.

## Coverage Requirements

For each public method, generate tests covering:

1. **Happy path** — normal successful execution with typical input
2. **Edge cases** — null inputs, empty collections, empty strings, boundary values, zero, negative numbers, BigDecimal precision
3. **Exception cases** — every explicitly thrown exception, every validation branch that rejects input
4. **Business rules** — every conditional branch that produces different outcomes based on logic

Skip testing for:

- Simple getters/setters with no logic
- `toString()`, `equals()`, `hashCode()` unless custom-implemented with business logic
- Private methods (test them through public method behavior)

## AssertJ Patterns

Use fluent AssertJ assertions. Prefer specific assertions over generic ones:

```java
// Value assertions
assertThat(result).isEqualTo(expected);
assertThat(result).isNotNull();
assertThat(result.getName()).isEqualTo("expected");

// Collection assertions
assertThat(results).hasSize(3);
assertThat(results).contains(expectedItem);
assertThat(results).extracting(Item::getName).containsExactly("A", "B", "C");
assertThat(results).isEmpty();

// BigDecimal — always use isEqualByComparingTo, never isEqualTo
assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("100.00"));

// Boolean — be direct
assertThat(result.isActive()).isTrue();

// Exception assertions
assertThatThrownBy(() -> service.process(invalidInput))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be empty");

// No exception expected (only when it adds clarity)
assertThatCode(() -> service.process(validInput)).doesNotThrowAnyException();
```

## Mockito Patterns

### Stubbing

- Use specific argument values, not `any()`, whenever possible — precise stubbing catches more bugs
- Use `any()` only when the argument value is irrelevant to the behavior being tested
- Use `lenient()` sparingly — if a stub seems unnecessary, the test may be too broad

```java
// Prefer specific
when(repository.findById(42L)).thenReturn(Optional.of(entity));

// Use any() only when the value doesn't matter for this test
when(mapper.toDto(any(Entity.class))).thenReturn(dto);
```

### Verification

- Verify interactions **only when the side effect is the behavior being tested** (e.g., verifying a save, a send, a delete)
- Do not verify calls to methods that are already asserted through return values
- Use `verifyNoMoreInteractions()` sparingly — only when proving nothing else happened is part of the requirement

```java
// Good: verifying a side effect IS the test
verify(notificationService).send(expectedNotification);

// Good: verifying something was NOT called
verify(repository, never()).delete(any());

// Bad: redundant — if findById wasn't called, the result assertion would fail anyway
verify(repository).findById(42L); // unnecessary if result is already asserted
```

## Parameterized Tests

Use `@ParameterizedTest` when testing the same behavior with multiple inputs:

```java
@ParameterizedTest
@NullAndEmptySource
void should_throwException_when_nameIsNullOrEmpty(String invalidName) {
    assertThatThrownBy(() -> service.createCustomer(invalidName))
            .isInstanceOf(IllegalArgumentException.class);
}

@ParameterizedTest
@CsvSource({
    "PENDING,   true",
    "SHIPPED,   false",
    "CANCELLED, false"
})
void should_returnCorrectCancellability_when_orderHasStatus(String status, boolean expected) {
    var order = createOrderWithStatus(OrderStatus.valueOf(status));

    var result = serviceUnderTest.isCancellable(order);

    assertThat(result).isEqualTo(expected);
}
```

Use parameterized tests when there are **3 or more** variations of the same behavior. For 2 variations, separate tests are clearer.

## Test Data

- Use descriptive variable names: `validCustomerId`, `emptyItemList`, `expiredOrder`
- Use local factory methods for complex objects: `createValidOrder()`, `createCustomerWith(String name)`
- No magic values without context — prefer named constants or descriptive variable names
- Use `var` for local variables where the type is obvious from context

```java
// Good
var validCustomerId = 42L;
var activeOrder = createOrderWithStatus(OrderStatus.ACTIVE);

// Bad
var id = 42L;
var order = new Order();
order.setStatus(OrderStatus.ACTIVE);
order.setId(42L);
order.setCustomerId(7L);
// ... 15 more lines of setup
```

If setup exceeds ~10 lines for a single object, create a helper method.

## What We Do NOT Do

- **No integration tests** — unit tests only, no Spring context loading
- **No `@SpringBootTest`** — if you need Spring, this is the wrong agent
- **No testing of third-party library behavior** — trust that Mockito, Jackson, etc. work
- **No testing of framework annotations** — we don't verify that `@Transactional` works
- **No test inheritance** — no abstract base test classes, each test class is self-contained
- **No field injection in tests** — always constructor injection via explicit `new` in `setUp()`
