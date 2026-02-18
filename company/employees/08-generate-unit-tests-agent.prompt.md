# Generate Unit Tests

## Task

Generate a complete JUnit 5 test class for the provided Java source file.

## Process

1. Read `testing-instructions.md` and `java-instructions.md` before writing any code
2. Analyze the class under test: identify all public methods, constructor dependencies, and branching logic
3. Plan the tests: for each public method, determine the happy path, edge cases, exception paths, and business rule branches
4. Generate the test class following the exact structure and conventions from the testing policy
5. Self-review: verify the generated test compiles, all public methods are covered, and naming conventions are followed
6. Provide the summary report

## Input

The user will provide the Java source file to test. If dependency interfaces are relevant, they will be included.

## Output

1. The complete test class (ready to compile)
2. A summary listing:
   - Test count
   - Methods covered
   - Skipped methods (with reason)
   - Notable edge cases
   - Anything that could not be tested in isolation (with explanation)
