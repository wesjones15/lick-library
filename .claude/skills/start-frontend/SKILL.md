---
description: Kill any process on port 5173 and start the lick_library_ui Vite dev server. Use this to (re)start the frontend.
---

# start-frontend

Kill whatever is on port 5173, then start the Vite dev server in the background.

## Usage

```
/start-frontend
```

## Steps

1. Kill any process on port 5173:
   ```
   lsof -ti:5173 | xargs kill -9 2>/dev/null; true
   ```
2. Start the dev server in the background from `/Users/wesleyjones/Documents/lick_library_ui`:
   ```
   cd /Users/wesleyjones/Documents/lick_library_ui && npm run dev
   ```
   Use `run_in_background: true` on the Bash tool call.
3. Wait a moment, then confirm the server is up by checking port 5173:
   ```
   sleep 3 && lsof -ti:5173
   ```
4. Report: URL the server is running at (typically http://localhost:5173)

## Notes
- `npm run dev` invokes `vite` — no wrapper script needed
- If the kill step finds nothing on 5173, that is fine — continue
