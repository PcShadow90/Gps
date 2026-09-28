export type RouteMode = "moto" | "curves" | "no_tolls";

export async function geocode(query: string) {
  const url = new URL("https://nominatim.openstreetmap.org/search");
  url.searchParams.set("format", "json");
  url.searchParams.set("limit", "5");
  url.searchParams.set("countrycodes", "pt");
  url.searchParams.set("q", query);
  const r = await fetch(url, { headers: { "User-Agent": "MotoGPS/1.0" } });
  if (!r.ok) throw new Error("Geocoding indisponível");
  return r.json();
}

export async function route(fromLat:number, fromLon:number, toLat:number, toLon:number, mode:RouteMode="moto") {
  const url = `https://router.project-osrm.org/route/v1/driving/${fromLon},${fromLat};${toLon},${toLat}`;
  const u = new URL(url);
  u.searchParams.set("overview","full");
  u.searchParams.set("geometries","geojson");
  u.searchParams.set("steps","true");
  u.searchParams.set("alternatives","true");
  const r = await fetch(u);
  if (!r.ok) throw new Error("Routing indisponível");
  const data = await r.json();
  return { mode, ...data };
}
