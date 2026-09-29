# AGENTS.md — MotoGPS

## Papel do repositório

PcShadow90/Gps é a **fonte única do produto MotoGPS**.

Os repositórios eve-chat-template e codespaces-react são independentes. Não copiar código desses templates para o MotoGPS.

## Responsabilidade por diretório

- app/ — aplicação Android.
- backend/src/ — lógica de domínio e serviços reutilizáveis.
- backend/db/ — schema PostgreSQL/PostGIS.
- api/ — únicos endpoints HTTP Vercel.
- admin/ — administração.
- docs/ — documentação.
- .github/workflows/ — CI.

## Regra de não duplicação

Não criar uma segunda implementação para o mesmo serviço.

Em particular:

- não recriar handlers em backend/api/;
- não criar outro vercel.json em backend/;
- não criar outro package.json ou tsconfig.json em backend/;
- reutilizar backend/src/routing.ts, models e módulos de domínio a partir de api/.

## API

A superfície HTTP canónica é /api.

Os handlers em api/ devem ser finos: validar entrada, chamar a lógica em backend/src/ e devolver a resposta HTTP.

## Android

Manter Kotlin + Jetpack Compose e preservar o comportamento existente de GPS, WebView, mapa, routing e voz.

## Segurança

Nunca versionar passwords, tokens, API keys, secrets Vercel, credenciais de base de dados ou chaves privadas.

## Ficheiros gerados

Não versionar build/, .gradle/, reports/, node_modules/, .vercel/ ou outros artefactos gerados.

## Processo

1. Ler este ficheiro e a documentação relevante.
2. Inspecionar dependências antes de criar novos componentes.
3. Reutilizar código existente.
4. Fazer alterações pequenas e verificáveis.
5. Executar validação TypeScript e/ou Android.
6. Rever o diff.
7. Fazer commit descritivo.

Documentação e comentários destinados à equipa devem usar PT-PT.
