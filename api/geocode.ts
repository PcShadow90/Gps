import type { VercelRequest, VercelResponse } from "@vercel/node";
import { setApiCors } from "../backend/src/cors";
import { geocode } from "../backend/src/routing";

export default async function handler(req: VercelRequest, res: VercelResponse) {
  setApiCors(req, res);
  if (req.method === "OPTIONS") return res.status(204).end();
  if (req.method !== "GET") {
    res.setHeader("Allow", "GET, OPTIONS");
    return res.status(405).json({ error: "Método não permitido" });
  }

  const query = typeof req.query.q === "string" ? req.query.q.trim() : "";
  if (!query) return res.status(400).json({ error: "Pesquisa vazia" });

  try {
    return res.status(200).json(await geocode(query));
  } catch {
    return res.status(502).json({ error: "Serviço de geocodificação indisponível" });
  }
}
