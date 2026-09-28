import type { VercelRequest, VercelResponse } from "@vercel/node";

export default async function handler(req: VercelRequest, res: VercelResponse) {
  const q = String(req.query.q ?? "").trim();
  if (!q) return res.status(400).json({ error: "Missing q" });

  const url = new URL("https://nominatim.openstreetmap.org/search");
  url.searchParams.set("q", q);
  url.searchParams.set("format", "jsonv2");
  url.searchParams.set("limit", "5");
  url.searchParams.set("addressdetails", "1");

  const response = await fetch(url, {
    headers: { "User-Agent": "MotoGPS/0.1 (+https://github.com/PcShadow90/Gps)" }
  });
  if (!response.ok) return res.status(502).json({ error: "Geocoding provider unavailable" });

  res.status(200).json(await response.json());
}
