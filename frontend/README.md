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

## Scripts

| Command | What it does |
| --- | --- |
| `npm start` | Vite dev server with the API proxy described above |
| `npm test` | Jest with coverage |
| `npx tsc --noEmit` | Type check (`vite build` does not type-check) |
| `npm run build` | Production build into `build/` (not `dist/`) |

To embed the UI in the backend jar, copy `build/*` into `executable/src/main/resources/public/` and run `mvn package` from the repository root.
