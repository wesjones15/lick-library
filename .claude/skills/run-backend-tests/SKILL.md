---
description: Run lick_library backend tests with Maven. Use this to verify position builder logic, lick service routing, or parser changes.
---

# run-backend-tests

Run the Spring Boot backend tests.

**IMPORTANT:** The Maven wrapper (`./mvnw`) does NOT exist in this repo. Always use `mvn` directly.

## Usage

```
/run-backend-tests [optional: package filter, e.g. "position" or "lick"]
```

## Steps

1. Change to the backend repo root: `/Users/wesleyjones/Documents/lick_library`
2. Run tests:
   - All tests: `mvn test`
   - Scoped to a package: `mvn test -Dtest="*<filter>*"` (e.g. `mvn test -Dtest="*Position*"`)
3. Report: pass/fail count, any failing test names and their error messages

## Notes
- Never use `./mvnw` — it does not exist
- Never use `--no-verify`
