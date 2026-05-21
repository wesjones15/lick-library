---
description: Kill any process on port 8080 and start the lick_library Spring Boot backend. Use this to (re)start the backend server.
---

# start-backend

Run `.claude/scripts/start-backend.sh` in the background, then confirm the server is up.

1. `bash /Users/wesleyjones/Documents/lick_library/.claude/scripts/start-backend.sh` — use `run_in_background: true`
2. `sleep 12 && lsof -ti:8080` — confirm process is listening
3. Report: running on http://localhost:8080 or any errors

Notes: never use `./mvnw`. H2 console at http://localhost:8080/h2-console.
