import type { VercelRequest, VercelResponse } from "@vercel/node";

export default async function handler(req: VercelRequest, res: VercelResponse) {
  const from = String(req.query.from ?? "").trim();
  const to = String(req.query.to ?? "").trim();
  if (!from || !to) return res.status(400).json({ error: "from and to are required as lon,lat" });

  const url = new URL(`https://router.project-osrm.org/route/v1/driving/${from};${to}`);
  url.searchParams.set("overview", "full");
  url.searchParams.set("geometries", "geojson");
  url.searchParams.set("steps", "true");

  const response = await fetch(url);
  if (!response.ok) return res.status(502).json({ error: "Routing provider unavailable" });

  res.status(200).json(await response.json());
}
