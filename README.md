# MotoGPS

Aplicação Android dedicada a motociclistas, com navegação GPS, rotas e modos específicos para condução de mota.

## Implementado
- Android nativo em Kotlin + Jetpack Compose.
- Localização GPS de alta precisão.
- Mapa Leaflet/OpenStreetMap.
- Pesquisa de destino.
- Cálculo de rota OSRM.
- Voz PT-PT.
- Modos Moto, Curvas e Sem Portagens.
- Base modular para comunidade, alertas, grupos, POI, offline e backend.

## Estrutura
- `app/`: aplicação Android.
- `app/src/main/assets/map.html`: mapa e motor inicial de rotas.
- Backend/admin serão adicionados em fases seguintes sem alterar o núcleo Android.

## Nota
O projeto foi preparado automaticamente no repositório GitHub existente `PcShadow90/Gps`.


## Fontes do projeto

### Código e desenvolvimento
- [Repositório GitHub MotoGPS](https://github.com/PcShadow90/Gps) — código-fonte principal.
- [Backend](https://github.com/PcShadow90/Gps/tree/main/backend) — fundação da API.
- [API](https://github.com/PcShadow90/Gps/tree/main/api) — endpoints do projeto.
- [Aplicação Android](https://github.com/PcShadow90/Gps/tree/main/app) — código da app.
- [Admin](https://github.com/PcShadow90/Gps/tree/main/admin) — base do painel administrativo.
- [PR #1 — Foundation API Vercel](https://github.com/PcShadow90/Gps/pull/1) — health check, geocoding Nominatim, routing OSRM e verificação TypeScript.
- [Branch `feat/backend-vercel-foundation`](https://github.com/PcShadow90/Gps/tree/feat/backend-vercel-foundation) — implementação inicial do backend.

### Serviços e tecnologias
- [Kotlin](https://kotlinlang.org/) — linguagem Android.
- [Jetpack Compose](https://developer.android.com/develop/ui/compose) — UI Android.
- [Google Location Services](https://developers.google.com/location-context/overview) — localização.
- [OpenStreetMap](https://www.openstreetmap.org/) — dados cartográficos.
- [Leaflet](https://leafletjs.com/) — mapa interativo.
- [Nominatim](https://nominatim.org/) — geocoding.
- [OSRM](https://project-osrm.org/) — cálculo de rotas.
- [Vercel](https://vercel.com/) — infraestrutura/backend prevista.
- [PostgreSQL](https://www.postgresql.org/) — base de dados prevista.
- [PostGIS](https://postgis.net/) — extensão geoespacial prevista.

### Gestão e design
- [Notion — Projeto MotoGPS](https://app.notion.com/p/3e9d5b586590814eab85e39412892e10?pvs=204) — gestão e documentação.
- [Canva — Protótipo mobile MotoGPS](https://www.canva.com/d/ro7uVbJARQ0evta) — design/prototipagem.
- Visualize — prototipagem referenciada na documentação; ligação ainda não disponível nesta integração.

### Estado das integrações
- Vercel: conta ligada, mas não existe atualmente um projeto MotoGPS configurado.
- Canva: protótipo mobile MotoGPS localizado e confirmado.
- GitHub: repositório principal confirmado como `PcShadow90/Gps`.
- Notion: página principal do projeto confirmada.
