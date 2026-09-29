# MotoGPS backend modules

The Vercel project uses the repository root as its project root. Its public function entry points live in `../api/`; this folder holds shared routing logic, data models, authentication scaffolding and the PostgreSQL/PostGIS schema.

Root API endpoints:

- `GET /api/health`
- `GET /api/geocode?q=...`
- `GET /api/route?fromLat=...&fromLon=...&toLat=...&toLon=...&mode=moto|curves|no_tolls`
- `GET /api/motogps?action=health|reports|pois|groups`

The `reports`, `pois` and `groups` actions are an in-memory prototype. They are not persistent or suitable for production data until database access, authentication and authorization are implemented.
