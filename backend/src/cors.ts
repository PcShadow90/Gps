import type { VercelRequest, VercelResponse } from "@vercel/node";

const ANDROID_ASSET_ORIGIN = "https://appassets.androidplatform.net";

export function setApiCors(req: VercelRequest, res: VercelResponse): boolean {
  const origin = req.headers.origin;
  if (origin !== ANDROID_ASSET_ORIGIN) return false;

  res.setHeader("Access-Control-Allow-Origin", ANDROID_ASSET_ORIGIN);
  res.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type");
  res.setHeader("Access-Control-Max-Age", "86400");
  res.setHeader("Vary", "Origin");
  return true;
}
