# Agent Company

AI-powered unit test generation using role-based agents.

## How It Works

1. Read `company/policy.md` for testing standards
2. Read `company/employees/tester.md` for the tester's role and constraints
3. Generate tests for classes in `src/main/java/`
4. Output tests to `src/test/java/`

## To Generate Tests

For a given class, the tester agent should:
1. Follow all conventions in `policy.md`
2. Operate within constraints defined in `tester.md`
3. Mock all dependencies
4. Cover all public methods