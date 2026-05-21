---
description: Kill any process on port 8080 and start the lick_library Spring Boot backend. Use this to (re)start the backend server.
---

# start-backend

Kill whatever is on port 8080, then start the Spring Boot app in the background.

## Usage

```
/start-backend
```

## Steps

1. Kill any process on port 8080:
   ```
   lsof -ti:8080 | xargs kill -9 2>/dev/null; true
   ```
2. Start the backend in the background from `/Users/wesleyjones/Documents/lick_library`:
   ```
   cd /Users/wesleyjones/Documents/lick_library && mvn spring-boot:run
   ```
   Use `run_in_background: true` on the Bash tool call.
3. Wait for startup, then confirm the server is up:
   ```
   sleep 8 && lsof -ti:8080
   ```
4. Report: confirmed running on http://localhost:8080, or any startup errors seen

## Notes
- Never use `./mvnw` — it does not exist; always use `mvn` directly
- Default port is 8080 (no explicit `server.port` in application.properties)
- H2 console available at http://localhost:8080/h2-console once running
