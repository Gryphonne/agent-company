# Review Unit Tests

## Task

Review the provided unit test class against the original source class, the testing policy, and mutation testing results. Provide a structured assessment with a clear verdict.

## Process

1. Read `testing-instructions.md` and `java-instructions.md`
2. Analyze the original source class to understand what SHOULD be tested
3. Review the test class against the policy checklist
4. Run PIT mutation testing scoped to the class under test (see Mutation Testing in agent definition)
5. Produce the structured review combining policy review and mutation results
6. If NEEDS REVISION: provide the corrected test class and re-validate it

## Input

The user will provide the generated test class and the original source file.

## Output

1. Policy review (naming, assertions, mocking, correctness)
2. Mutation report (kill rate, surviving mutants with analysis)
3. Verdict: PASS, NEEDS REVISION, or FAIL
4. If NEEDS REVISION: corrected and re-validated test class