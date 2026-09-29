# MotoGPS

Aplicação Android dedicada a motociclistas, com navegação GPS, rotas e modos específicos para condução de mota.

## Protótipo Android disponível
- App Android em Kotlin + Jetpack Compose, com mapa Leaflet/OpenStreetMap numa WebView local.
- Pedido de localização precisa/aproximada com explicação antes da permissão e uma posição inicial para centrar o mapa.
- Pesquisa de destinos em Portugal e cálculo de rotas através dos serviços públicos Nominatim e OSRM.
- Orientações por voz PT-PT e atualização de posição/velocidade durante uma sessão de navegação.
- Preferência por curvas entre as alternativas devolvidas pelo OSRM e pedido de exclusão de portagens quando suportado pelo fornecedor.
- Serviço Android de localização em primeiro plano, com controlo para parar a navegação e informação sobre o efeito da recusa de notificações.
- Estados de localização e de cálculo de rota apresentados no ecrã.

## Estado e limites
Esta versão é um protótipo funcional de navegação e ainda não está pronta para publicação. Os modos e as rotas dependem da cobertura e das capacidades do serviço OSRM público; não são garantia de rotas seguras ou adequadas a motociclos. A preferência por curvas é uma heurística aplicada às alternativas disponíveis.

O Android ainda não usa o backend Vercel do repositório. O fluxo actual envia a pesquisa do destino ao Nominatim e as coordenadas de origem/destino ao OSRM. Não há autenticação, armazenamento persistente, alertas comunitários, grupos nem mapas offline implementados na app.

Antes de qualquer release, falta validar a experiência num telemóvel real (permissões, localização aproximada, ecrã bloqueado, perda de sinal, bateria e serviço de localização), rever o tratamento de privacidade dos fornecedores de mapas e concluir a integração de backend/persistência. Um APK de debug não é uma versão de publicação.

## Estrutura
- `app/`: aplicação Android.
- `app/src/main/assets/map.html`: mapa e motor inicial de rotas.
- `backend/` e `api/`: fundação de API, ainda sem integração com o cliente Android nem armazenamento persistente.
- `admin/`: base inicial do painel administrativo.

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
