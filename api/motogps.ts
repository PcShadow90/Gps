import type { VercelRequest, VercelResponse } from "@vercel/node";
import { geocode, route } from "../backend/src/routing";
import type { Report, Poi, Group } from "../backend/src/models";

const reports: Report[] = [];
const pois: Poi[] = [];
const groups: Group[] = [];

export default async function handler(req: VercelRequest, res: VercelResponse) {
  try {
    const action=String(req.query.action||"geocode");
    if(action==="geocode" && typeof req.query.q==="string") return res.status(200).json(await geocode(req.query.q));
    if(action==="route"){
      const n=["fromLat","fromLon","toLat","toLon"].map(k=>Number(req.query[k]));
      if(n.some(Number.isNaN)) return res.status(400).json({error:"Coordenadas inválidas"});
      return res.status(200).json(await route(n[0],n[1],n[2],n[3],String(req.query.mode||"moto") as any));
    }
    if(action==="reports") return res.status(200).json(reports);
    if(action==="pois") return res.status(200).json(pois);
    if(action==="groups") return res.status(200).json(groups);
    if(action==="health") return res.status(200).json({ok:true,service:"MotoGPS API",time:new Date().toISOString()});
    return res.status(400).json({error:"Ação inválida"});
  } catch(e) { return res.status(502).json({error:e instanceof Error?e.message:"Erro interno"}); }
}