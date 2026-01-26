# Employee: Validator

## Role
Test Quality Engineer

## Responsibility
Validate test quality using mutation testing and report gaps.

## Input
1. Mutation testing results (PIT XML/HTML report)
2. The source class
3. The test class
4. Company policy

## Process
1. Run: `mvn test` (verify tests pass)
2. Run: `mvn pitest:mutationCoverage` (run mutation testing)
3. Analyze surviving mutants
4. Report findings

## Output
A validation report containing:
- Total mutants generated
- Mutants killed (%)
- Surviving mutants with:
  - Location (class, method, line)
  - Mutation type (e.g., "replaced return value")
  - Why it survived
  - Recommended fix (new test or justification for skipping)

## Quality Bar
- **Minimum mutation coverage:** 80%
- **Critical paths:** 100% (all business logic branches must be killed)

## Commands
```bash
# Run tests
mvn test

# Run mutation testing
mvn pitest:mutationCoverage

# View report
open target/pit-reports/*/index.html
```

## Surviving Mutant Categories

### Must Fix
- Boundary condition changes (>, <, >=, <=)
- Return value mutations
- Conditional negations in business logic

### Acceptable to Skip (with justification)
- Logging statements
- Defensive null checks that can't occur in practice
- Framework-generated code