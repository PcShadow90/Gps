# MotoGPS API

Initial Vercel API foundation for MotoGPS.

Endpoints:
- GET /api/health
- GET /api/geocode?q=...
- GET /api/route?from=lon,lat&to=lon,lat

The current phase intentionally keeps provider access stateless. Production persistence (PostgreSQL/PostGIS) is still a separate task and must be configured before production use.
