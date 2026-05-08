# Frontend Plan — Lick Library

React 18 + TypeScript + Vite + Tailwind CSS.

---

## API contract (TypeScript types)

```typescript
interface LickSummary {
  id: string;
  rawTab: string;
  intervalDisplayString: string;
  mode: string | null;
  positions: null;
}

interface LickDetail {
  id: string;
  rawTab: string;
  intervalDisplayString: string;
  mode: string;
  positions: PositionResponse[];
}

interface PositionResponse {
  tabString: string;   // rendered by backend
}

interface UploadRequest {
  rawTab: string;
  mode?: string;
}
```

---

## Pages

### `/` — Library (list + upload)
- Left/top: upload form (textarea for raw tab, optional mode dropdown, submit)
- Main area: list of lick cards — shows `rawTab` in monospace, `intervalDisplayString`, `mode`
- Clicking a card navigates to `/lick/:id`

### `/lick/:id` — Detail
- Key selector dropdown (all 12 notes: C, C#, D, D#, E, F, F#, G, G#, A, A#, B)
- On key change: fetch `GET /api/lick/{id}?key={key}` and display positions
- Each position rendered as a monospace tab block
- Back link to `/`

---

## Components

```
src/
├── api/
│   └── client.ts          # typed fetch wrappers for all 3 endpoints
├── components/
│   ├── LickCard.tsx        # rawTab + intervalDisplayString + mode chip
│   ├── LickList.tsx        # maps over LickSummary[]
│   ├── UploadForm.tsx      # textarea, mode select, submit
│   ├── KeySelector.tsx     # controlled dropdown for 12 notes
│   └── PositionTab.tsx     # single <pre> block with tabString
└── pages/
    ├── LibraryPage.tsx     # / route
    └── DetailPage.tsx      # /lick/:id route
```

---

## Routing

React Router v6. Two routes: `/` and `/lick/:id`.

---

## State

No global state library needed. Each page manages its own state with `useState` + `useEffect`. Upload success on `/` triggers a re-fetch of the lick list.

---

## Repo setup steps

```bash
npm create vite@latest lick-library-ui -- --template react-ts
cd lick-library-ui
npm install
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
npm install react-router-dom
```
