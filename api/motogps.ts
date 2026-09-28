import type { VercelRequest, VercelResponse } from "@vercel/node";
import { geocode, route } from "../backend/src/routing";

export default async function handler(req: VercelRequest, res: VercelResponse) {
  try {
    const { action="geocode", q, fromLat, fromLon, toLat, toLon, mode="moto" } = req.query;
    if (action === "geocode" && typeof q === "string") return res.status(200).json(await geocode(q));
    if (action === "route") {
      const nums = [fromLat,fromLon,toLat,toLon].map(Number);
      if (nums.some(Number.isNaN)) return res.status(400).json({error:"Coordenadas inválidas"});
      return res.status(200).json(await route(nums[0],nums[1],nums[2],nums[3],String(mode) as any));
    }
    return res.status(400).json({error:"Ação inválida"});
  } catch (e) {
    return res.status(502).json({error:e instanceof Error ? e.message : "Erro interno"});
  }
}