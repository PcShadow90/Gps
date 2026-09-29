import type { VercelRequest, VercelResponse } from "@vercel/node";
import { setApiCors } from "../backend/src/cors.js";

export default function handler(req: VercelRequest, res: VercelResponse) {
  setApiCors(req, res);
  if (req.method === "OPTIONS") return res.status(204).end();
  if (req.method !== "GET") {
    res.setHeader("Allow", "GET, OPTIONS");
    return res.status(405).json({ error: "Método não permitido" });
  }

  return res.status(200).json({
    ok: true,
    service: "motogps-api",
    storage: "memory",
    productionDatabaseConfigured: false,
    version: "0.1.0"
  });
}
