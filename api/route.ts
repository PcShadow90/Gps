import type { VercelRequest, VercelResponse } from "@vercel/node";
import { setApiCors } from "../backend/src/cors.js";
import { route } from "../backend/src/routing.js";
import type { RouteMode } from "../backend/src/routing.js";

const ROUTE_MODES: RouteMode[] = ["moto", "curves", "no_tolls"];

export default async function handler(req: VercelRequest, res: VercelResponse) {
  setApiCors(req, res);
  if (req.method === "OPTIONS") return res.status(204).end();
  if (req.method !== "GET") {
    res.setHeader("Allow", "GET, OPTIONS");
    return res.status(405).json({ error: "Método não permitido" });
  }

  const coordinates = ["fromLat", "fromLon", "toLat", "toLon"].map((key) => {
    const value = req.query[key];
    return typeof value === "string" && value.trim() ? Number(value) : Number.NaN;
  });
  const [fromLat, fromLon, toLat, toLon] = coordinates;
  if (
    !coordinates.every(Number.isFinite) || Math.abs(fromLat) > 90 || Math.abs(toLat) > 90 ||
    Math.abs(fromLon) > 180 || Math.abs(toLon) > 180
  ) return res.status(400).json({ error: "Coordenadas inválidas" });

  const modeValue = typeof req.query.mode === "string" ? req.query.mode : "moto";
  if (!ROUTE_MODES.includes(modeValue as RouteMode)) {
    return res.status(400).json({ error: "Modo de rota inválido" });
  }

  try {
    return res.status(200).json(
      await route(fromLat, fromLon, toLat, toLon, modeValue as RouteMode)
    );
  } catch {
    return res.status(502).json({ error: "Serviço de rotas indisponível" });
  }
}
