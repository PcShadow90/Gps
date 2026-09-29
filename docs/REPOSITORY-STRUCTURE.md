# Repository structure

## Repositories

| Repository | Role | Production code |
|---|---|---|
| PcShadow90/Gps | MotoGPS product | Yes |
| PcShadow90/eve-chat-template | Reusable eve/Next.js chat template | No |
| PcShadow90/codespaces-react | Generic React/Codespaces sandbox | No |

## MotoGPS canonical layout

- app/ — Android application (Kotlin + Compose)
- app/src/main/assets/ — Leaflet/OpenStreetMap map layer
- backend/src/ — shared backend/domain logic
- backend/db/ — database schema
- api/ — Vercel HTTP entrypoints only
- admin/ — administration UI
- docs/ — architecture and operational documentation
- .github/workflows/ — CI

## Rules

- api/ is the only canonical location for Vercel HTTP handlers.
- backend/src/ contains reusable server/domain logic; it is not a second API surface.
- Android code stays inside app/.
- Generated Android/Gradle output is never committed.
- Root package.json, tsconfig.json and vercel.json are the canonical Node/Vercel configuration.
- Do not recreate backend/package.json, backend/tsconfig.json or backend/vercel.json unless the project is intentionally split into a separate deployable service.
- New services must have a documented owner and a single canonical implementation.

## Template repositories

eve-chat-template and codespaces-react are development templates. They are not dependencies of MotoGPS and their source code should not be copied into MotoGPS.

When functionality is genuinely reusable, extract a small package or document the interface instead of copying application code between repositories.
