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

O Android aceita a URL base da API Vercel através da propriedade Gradle `motogpsApiBaseUrl` ou da variável de ambiente `MOTOGPS_API_BASE_URL`. Quando a propriedade está vazia, o protótipo continua a chamar diretamente os serviços públicos Nominatim e OSRM. A app não deve usar a API Vercel enquanto a proteção por autenticação estiver activa, porque não pode concluir o início de sessão da equipa.

Os endpoints da raiz do repositório são `/api/health`, `/api/geocode` e `/api/route`; o painel administrativo usa `/api/motogps`. Os dados de alertas, POIs e grupos ainda vivem apenas em memória. Não há autenticação de utilizadores nem armazenamento persistente, e os módulos de comunidade e mapas offline ainda não estão implementados na app.

Antes de qualquer release, falta validar a experiência num telemóvel real (permissões, localização aproximada, ecrã bloqueado, perda de sinal, bateria e serviço de localização), rever o tratamento de privacidade dos fornecedores de mapas e concluir a integração de backend/persistência. Um APK de debug não é uma versão de publicação.

## Estrutura
- `app/`: aplicação Android.
- `app/src/main/assets/map.html`: mapa e motor inicial de rotas.
- `api/`: endpoints Vercel usados pelo painel e configuráveis no cliente Android.
- `backend/`: lógica partilhada de geocoding/rotas, modelos, autenticação futura e esquema PostgreSQL/PostGIS.
- `admin/`: base inicial do painel administrativo.

## Fontes do projeto

### Código e desenvolvimento
- [Repositório GitHub MotoGPS](https://github.com/PcShadow90/Gps) — código-fonte principal.
- [Backend](https://github.com/PcShadow90/Gps/tree/main/backend) — fundação da API.
- [API](https://github.com/PcShadow90/Gps/tree/main/api) — endpoints do projeto.
- [Aplicação Android](https://github.com/PcShadow90/Gps/tree/main/app) — código da app.
- [Admin](https://github.com/PcShadow90/Gps/tree/main/admin) — base do painel administrativo.
- [PR #1 — Foundation API Vercel](https://github.com/PcShadow90/Gps/pull/1) — fundação inicial da API.
- [Branch `feat/navigation-flow-polish`](https://github.com/PcShadow90/Gps/tree/feat/navigation-flow-polish) — fluxo Android e alinhamento dos endpoints.

### Serviços e tecnologias
- [Kotlin](https://kotlinlang.org/) — linguagem Android.
- [Jetpack Compose](https://developer.android.com/develop/ui/compose) — UI Android.
- [Google Location Services](https://developers.google.com/location-context/overview) — localização.
- [OpenStreetMap](https://www.openstreetmap.org/) — dados cartográficos.
- [Leaflet](https://leafletjs.com/) — mapa interativo.
- [Nominatim](https://nominatim.org/) — geocoding.
- [OSRM](https://project-osrm.org/) — cálculo de rotas.
- [Projeto MotoGPS na Vercel](https://vercel.com/laser4yme-4404/gps) — projeto `gps`, ligado ao repositório GitHub.
- [PostgreSQL](https://www.postgresql.org/) — base de dados prevista.
- [PostGIS](https://postgis.net/) — extensão geoespacial prevista.

### Gestão e design
- [Notion — Projeto MotoGPS](https://app.notion.com/p/3e9d5b586590814eab85e39412892e10?pvs=204) — gestão e documentação.
- [Canva — Protótipo mobile MotoGPS](https://www.canva.com/d/25Nd7i91p0ow9dD) — design/prototipagem.
- [Canva — Revisão técnica MotoGPS](https://www.canva.com/design/DAHWkEvLR3E/view) — auditoria técnica actualizada; abrir pelo endereço permanente do design e pedir acesso caso o Canva indique falta de permissões.
- Visualize — prototipagem referenciada na documentação; URL ainda não fornecido.

### Estado das integrações
- GitHub: o repositório configurado é `PcShadow90/Gps`; `PcShadow/motogps` não existe nesta conta.
- Vercel: projecto `gps` ligado ao GitHub; Output Directory alinhado para `admin/`. As prévias continuam protegidas por autenticação.
- API Vercel: o endpoint principal está protegido por autenticação Vercel. A integração da app só fica activa quando existir uma URL de produção acessível pelo dispositivo e esta for passada a `motogpsApiBaseUrl`/`MOTOGPS_API_BASE_URL`.
- Canva: protótipo confirmado e relatório técnico actualizado; a sessão actual do Canva não tem acesso ao design, pelo que o proprietário deve partilhá-lo com a conta utilizada.
- Notion: página principal do projecto confirmada e a actualizar com as configurações efectivamente verificadas.

## Configurar o cliente Android com a API

Depois de publicar um endpoint HTTPS acessível sem autenticação Vercel pelo dispositivo, defina a propriedade Gradle antes de gerar o APK:

```powershell
$env:MOTOGPS_API_BASE_URL = "https://<dominio-publico-da-api>"
gradle assembleDebug
```

Também é possível usar `-PmotogpsApiBaseUrl=https://<dominio-publico-da-api>`. Não inclua tokens de bypass da Vercel na app Android: seriam extraíveis do APK. Se não definir a URL, o protótipo continua a usar directamente Nominatim e OSRM.
