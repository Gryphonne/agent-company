
# Generate Unit Tests

## Task

Generate a complete JUnit 5 test class for the provided Java source file. Validate it compiles, passes, and achieves coverage before delivering.

## Process

1. Read `testing-instructions.md` and `java-instructions.md` before writing any code
2. Analyze the class under test: identify all public methods, constructor dependencies, and branching logic
3. Plan the tests: for each public method, determine the happy path, edge cases, exception paths, and business rule branches
4. Generate the test class following the exact structure and conventions from the testing policy
5. **Validate**: compile the test class, run it, check coverage — fix and repeat until clean (see Validation Workflow in agent definition)
6. Provide the summary report

## Input

The user will provide the Java source file to test. If dependency interfaces are relevant, they will be included.

## Output

1. The complete test class (validated: compiles, passes, coverage checked)
2. A summary listing:
   - Test count
   - Methods covered
   - Skipped methods (with reason)
   - Notable edge cases
   - Compilation/run attempts needed
   - Final coverage for the class under test
   - Anything that could not be tested in isolation (with explanation)