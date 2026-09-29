# AGENTS.md — MotoGPS

## Objectivo

Este ficheiro define as regras de trabalho para agentes de IA e contribuidores que alterem o projecto **MotoGPS**.

O MotoGPS é uma aplicação Android para motociclistas, com navegação GPS, pesquisa de destinos, cálculo de rotas e modos específicos de condução.

## Repositório

- Repositório principal: `PcShadow90/Gps`
- Branch principal: `main`
- Aplicação Android: `app/`
- Mapa/motor inicial: `app/src/main/assets/map.html`
- Backend: `backend/`
- API: `api/`
- Administração: `admin/`

## Estado funcional de referência

A implementação existente inclui:

- Android nativo em Kotlin.
- Jetpack Compose para a interface.
- Localização GPS de alta precisão.
- Leaflet/OpenStreetMap para cartografia.
- Pesquisa de destinos.
- Routing através de OSRM.
- Voz em PT-PT.
- Modos de condução Moto, Curvas e Sem Portagens.
- Base modular para funcionalidades futuras de comunidade, alertas, grupos, POI, modo offline e backend.

## Regras obrigatórias

### 1. Preservação do projecto

- Não apagar, substituir ou reestruturar funcionalidades existentes sem necessidade técnica clara.
- Antes de alterar uma área, inspeccionar o código e compreender as dependências existentes.
- Preferir alterações pequenas, reversíveis e compatíveis com o código actual.
- Não criar duplicações de componentes, serviços, modelos ou configurações quando já existir uma implementação reutilizável.

### 2. Android

- Manter Kotlin como linguagem principal.
- Manter Jetpack Compose para novas interfaces, salvo quando uma API Android existente exigir outra abordagem.
- Preservar a compatibilidade com a arquitectura Android já implementada.
- Evitar alterações arbitrárias ao `AndroidManifest.xml`, permissões, configurações Gradle ou SDK.
- Qualquer mudança relacionada com localização deve considerar permissões, ciclo de vida e consumo de bateria.

### 3. GPS e navegação

- O estado da localização deve ser tratado como potencialmente dinâmico e impreciso.
- Não assumir que existe sinal GPS ou ligação de rede.
- Rotas e instruções devem degradar de forma segura quando um serviço externo estiver indisponível.
- Não expor chaves, tokens ou credenciais no código-fonte.
- Não alterar silenciosamente a lógica de routing, geocoding ou mapa sem verificar o impacto na navegação.

### 4. Mapa

- O mapa actual utiliza Leaflet + OpenStreetMap em `app/src/main/assets/map.html`.
- Alterações ao mapa devem preservar a comunicação existente entre Android e a camada web.
- Não remover configurações ou funções JavaScript existentes sem verificar todas as chamadas Android dependentes delas.
- Respeitar as políticas de utilização e atribuição dos serviços de mapas utilizados.

### 5. Serviços externos

Serviços actualmente referenciados no projecto:

- Nominatim — geocoding.
- OSRM — cálculo de rotas.
- OpenStreetMap — dados cartográficos.
- Vercel — backend/infraestrutura prevista.
- PostgreSQL/PostGIS — base de dados prevista.

Qualquer integração nova deve:
1. ser documentada;
2. ter tratamento de erros;
3. evitar credenciais hardcoded;
4. possuir uma estratégia de fallback quando aplicável.

### 6. Backend e API

- Manter separação entre app Android, API, backend e painel administrativo.
- Não colocar lógica de negócio exclusivamente no cliente quando essa lógica exigir validação no servidor.
- Validar entradas recebidas pela API.
- Não guardar segredos em ficheiros versionados.
- Alterações à API devem preservar compatibilidade ou documentar claramente eventuais breaking changes.

### 7. Segurança

Nunca incluir no repositório:

- palavras-passe;
- API keys;
- tokens OAuth;
- secrets de Vercel;
- credenciais de bases de dados;
- chaves privadas;
- dados pessoais reais desnecessários.

Usar variáveis de ambiente, secret managers ou mecanismos equivalentes.

### 8. Qualidade de código

- Usar nomes claros e consistentes.
- Manter funções pequenas quando possível.
- Evitar código morto e imports não utilizados.
- Não introduzir dependências apenas para resolver problemas simples.
- Documentar decisões técnicas não óbvias.
- Manter comentários e documentação em **PT-PT** quando forem destinados à equipa do projecto.

### 9. Testes e validação

Antes de considerar uma alteração concluída:

- compilar o componente afectado;
- executar os testes existentes quando disponíveis;
- verificar erros de lint/análise estática quando configurados;
- validar que a funcionalidade anterior continua operacional;
- verificar especialmente alterações a GPS, mapa, routing e voz.

Quando uma validação não puder ser executada, registar explicitamente essa limitação.

### 10. Git

- Fazer commits pequenos e com mensagens descritivas.
- Não misturar refactorizações não relacionadas com alterações funcionais.
- Não fazer force push nem reescrever histórico sem pedido explícito.
- Não apagar branches ou ficheiros apenas por limpeza sem verificar dependências.

## Processo recomendado para agentes

1. Ler este ficheiro.
2. Ler o README e a documentação relevante.
3. Inspeccionar os ficheiros directamente envolvidos.
4. Identificar dependências e impacto.
5. Implementar a menor alteração necessária.
6. Executar validações.
7. Rever o diff.
8. Fazer commit apenas com a alteração relacionada.
9. Registar limitações, testes e decisões relevantes.

## Prioridade das decisões

Em caso de conflito, seguir esta ordem:

1. Segurança e integridade dos dados.
2. Funcionamento actual da aplicação.
3. Compatibilidade.
4. Requisitos explícitos do projecto.
5. Simplicidade e manutenção.
6. Melhorias futuras.

## Documentação do projecto

Fontes de referência mantidas no README:

- GitHub: https://github.com/PcShadow90/Gps
- Notion — Projecto MotoGPS.
- Canva — Protótipo mobile MotoGPS.

Este documento é um guia operacional para agentes. O README e o código actual continuam a ser a referência principal sobre o estado real da implementação.
