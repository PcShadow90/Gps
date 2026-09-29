import type { VercelRequest, VercelResponse } from "@vercel/node";
import { geocode, route } from "../backend/src/routing.js";
import type { Report, Poi, Group } from "../backend/src/models.js";

const reports: Report[] = [];
const pois: Poi[] = [];
const groups: Group[] = [];

function methodAllowed(req: VercelRequest, res: VercelResponse, methods: string[]) {
  if (!methods.includes(req.method || "")) {
    res.setHeader("Allow", methods.join(", "));
    res.status(405).json({error:"Método não permitido"});
    return false;
  }
  return true;
}

export default async function handler(req: VercelRequest, res: VercelResponse) {
  try {
    const action=String(req.query.action||"health");

    if (action==="health") {
      return res.status(200).json({
        ok:true,
        service:"MotoGPS API",
        storage:"memory",
        productionDatabaseConfigured:false,
        time:new Date().toISOString()
      });
    }

    if (action==="geocode") {
      if (!methodAllowed(req,res,["GET"])) return;
      if (typeof req.query.q!=="string" || !req.query.q.trim()) return res.status(400).json({error:"Pesquisa vazia"});
      return res.status(200).json(await geocode(req.query.q));
    }

    if (action==="route") {
      if (!methodAllowed(req,res,["GET"])) return;
      const n=["fromLat","fromLon","toLat","toLon"].map(k=>Number(req.query[k]));
      if(n.some(Number.isNaN)) return res.status(400).json({error:"Coordenadas inválidas"});
      const mode=String(req.query.mode||"moto");
      if(!["moto","curves","no_tolls"].includes(mode)) return res.status(400).json({error:"Modo de rota inválido"});
      return res.status(200).json(await route(n[0],n[1],n[2],n[3],mode as "moto"|"curves"|"no_tolls"));
    }

    if (action==="reports") {
      if (req.method==="GET") return res.status(200).json(reports);
      if (req.method==="POST") {
        const b=typeof req.body==="string"?JSON.parse(req.body):req.body;
        const item:Report={
          id:crypto.randomUUID(),
          type:String(b?.type||"outro"),
          description:b?.description?String(b.description):undefined,
          lat:Number(b?.lat),
          lon:Number(b?.lon),
          createdAt:new Date().toISOString()
        };
        if(!Number.isFinite(item.lat)||!Number.isFinite(item.lon)) return res.status(400).json({error:"Localização inválida"});
        reports.push(item);
        return res.status(201).json(item);
      }
      return res.status(405).json({error:"Método não permitido"});
    }

    if (action==="pois") {
      if (!methodAllowed(req,res,["GET"])) return;
      return res.status(200).json(pois);
    }

    if (action==="groups") {
      if (req.method==="GET") return res.status(200).json(groups);
      if (req.method==="POST") {
        const b=typeof req.body==="string"?JSON.parse(req.body):req.body;
        const item:Group={id:crypto.randomUUID(),name:String(b?.name||"Novo grupo"),ownerId:b?.ownerId?String(b.ownerId):undefined,memberCount:1};
        groups.push(item);
        return res.status(201).json(item);
      }
      return res.status(405).json({error:"Método não permitido"});
    }

    return res.status(404).json({error:"Ação inexistente"});
  } catch(e) {
    return res.status(500).json({error:e instanceof Error?e.message:"Erro interno"});
  }
}