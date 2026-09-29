# MotoGPS

Aplicação Android dedicada a motociclistas, com navegação GPS, rotas e modos específicos para condução de mota.

## Estado

O repositório é a **fonte única do produto MotoGPS**. A organização foi consolidada para evitar duplicação entre Android, API e backend.

## Componentes

- Android nativo em Kotlin + Jetpack Compose.
- Localização GPS de alta precisão.
- Mapa Leaflet/OpenStreetMap.
- Pesquisa de destino.
- Cálculo de rota através de OSRM.
- Voz PT-PT.
- Modos Moto, Curvas e Sem Portagens.
- Backend modular para comunidade, alertas, grupos, POI e persistência futura.
- API Vercel.
- Base de dados PostgreSQL/PostGIS prevista.
- Painel administrativo.

## Estrutura canónica

- app/ — aplicação Android.
- app/src/main/assets/map.html — mapa e integração web.
- backend/src/ — lógica de domínio e serviços reutilizáveis.
- backend/db/ — schema PostgreSQL/PostGIS.
- api/ — únicos endpoints HTTP Vercel.
- admin/ — painel administrativo.
- docs/ — arquitectura e documentação operacional.
- .github/workflows/ — CI.

Não existem endpoints duplicados em backend/api/. A configuração Node/Vercel é mantida na raiz.

Consulte [docs/REPOSITORY-STRUCTURE.md](docs/REPOSITORY-STRUCTURE.md) para as regras de organização.

## Desenvolvimento

Node/Vercel:

```bash
npm install
npm run check
npm run dev
```

Android:

```bash
./gradlew assembleDebug
```

## Fontes do projeto

### Código e desenvolvimento

- [Repositório GitHub MotoGPS](https://github.com/PcShadow90/Gps) — código-fonte principal.
- [API](https://github.com/PcShadow90/Gps/tree/main/api) — endpoints HTTP.
- [Backend](https://github.com/PcShadow90/Gps/tree/main/backend) — lógica de domínio e schema.
- [Aplicação Android](https://github.com/PcShadow90/Gps/tree/main/app) — código da app.
- [Admin](https://github.com/PcShadow90/Gps/tree/main/admin) — painel administrativo.
- [AGENTS.md](AGENTS.md) — regras para agentes e contribuidores.

### Serviços e tecnologias

- Kotlin
- Jetpack Compose
- Google Location Services
- OpenStreetMap
- Leaflet
- Nominatim
- OSRM
- Vercel
- PostgreSQL
- PostGIS

### Gestão e design

- Notion — Projeto MotoGPS.
- Canva — Protótipo mobile MotoGPS.
