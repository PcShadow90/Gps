export type RouteMode = "moto" | "curves" | "no_tolls";

export async function geocode(query: string) {
  const url = new URL("https://nominatim.openstreetmap.org/search");
  url.searchParams.set("format", "json");
  url.searchParams.set("limit", "5");
  url.searchParams.set("countrycodes", "pt");
  url.searchParams.set("q", query);
  const r = await fetch(url, { headers: { "User-Agent": "MotoGPS/1.0 (navigation app)" } });
  if (!r.ok) throw new Error("Geocoding indisponível");
  return r.json();
}

export async function route(
  fromLat:number, fromLon:number, toLat:number, toLon:number, mode:RouteMode="moto"
) {
  if (
    ![fromLat,fromLon,toLat,toLon].every(Number.isFinite) ||
    Math.abs(fromLat) > 90 || Math.abs(toLat) > 90 ||
    Math.abs(fromLon) > 180 || Math.abs(toLon) > 180
  ) throw new Error("Coordenadas inválidas");
  const url = new URL(`https://router.project-osrm.org/route/v1/driving/${fromLon},${fromLat};${toLon},${toLat}`);
  url.searchParams.set("overview","full");
  url.searchParams.set("geometries","geojson");
  url.searchParams.set("steps","true");
  url.searchParams.set("alternatives","true");
  if (mode === "no_tolls") url.searchParams.set("exclude", "toll");
  const r = await fetch(url);
  if (!r.ok) throw new Error("Routing indisponível");
  const data = await r.json();
  return { mode, routingProvider:"OSRM", ...data };
}
