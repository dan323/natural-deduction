# Natural deduction frontend

React / TypeScript UI built with Vite. It talks to the Spring Boot backend through relative URLs (`/logic/...`).

## Development

The UI needs the backend, so run both:

1. Start the backend from the repository root (it listens on port 8080):

   ```bash
   mvn -B clean install
   java -jar executable/target/executable-0.1-SNAPSHOT.jar
   ```

2. Start the Vite dev server from this folder:

   ```bash
   npm ci
   npm start
   ```

   Open the URL Vite prints (by default [http://localhost:5173](http://localhost:5173)). The dev server proxies `/logic/...` to `http://localhost:8080` (see `server.proxy` in `vite.config.ts`), so no CORS setup is needed.

## Logics

The UI offers classical, intuitionistic and modal logic (`LOGICS` in `src/constant.ts`). The logic is picked in the New
Proof dialog, or on the empty page for "Try an example" and the exercises, and is named next to the goal. Intuitionistic
logic has no ¬E rule and no solver, so the Menu does not offer Solve for it. In modal logic every step holds in a state:
the premises start in `s0` (except a relation such as `s0 <= s1`, which holds in none), and the proof table shows a
State column. Known limitation: the backend's `done` does not look at the state yet, so a modal proof that derives the
goal formula at level 0 in another state (say `p` in `s1` by `[]E` from `[] p` and `s0 <= s1`) is shown as complete
although the goal holds in `s0`. The exercises cannot reach this (their premises are all in `s0` and none is a
relation); the fix belongs in the backend's modal `isDone`.

The rule menu is built from `GET /logic/{logic}/actions`, which lists each action with the kinds of input it takes
(`INT`, `EXPRESSION`, `STATE`), and the Solve button calls `POST /logic/{logic}/solve`; see `docs/API.md`.

## Scripts

| Command | What it does |
| --- | --- |
| `npm start` | Vite dev server with the API proxy described above |
| `npm test` | Jest with coverage |
| `npm run typecheck` | Type check with `tsc --noEmit` (`vite build` does not type-check) |
| `npm run build` | Production build into `build/` (not `dist/`) |

To embed the UI in the backend jar, copy `build/*` into `executable/src/main/resources/public/` and run `mvn package` from the repository root.
