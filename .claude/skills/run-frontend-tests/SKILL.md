---
description: Run lick_library_ui type-check and lint. Use this to verify TypeScript correctness and catch lint errors after frontend changes.
---

# run-frontend-tests

Run the frontend static checks (TypeScript compiler + ESLint). No test runner is configured — these are the available verification steps.

## Usage

```
/run-frontend-tests
```

## Steps

1. Frontend root: `/Users/wesleyjones/Documents/lick_library_ui`
2. Run TypeScript type-check:
   ```
   cd /Users/wesleyjones/Documents/lick_library_ui && npx tsc --noEmit
   ```
3. Run ESLint:
   ```
   cd /Users/wesleyjones/Documents/lick_library_ui && npm run lint
   ```
4. Report: any type errors or lint violations; confirm clean if none

## Notes
- No vitest/jest configured — tsc + lint are the only checks
- Run both steps even if one fails so the full error picture is visible
