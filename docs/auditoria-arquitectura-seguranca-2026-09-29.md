# MotoGPS — auditoria técnica e plano de validação

**Data da revisão:** 29 de setembro de 2026  
**Repositório:** [PcShadow90/Gps](https://github.com/PcShadow90/Gps), branch `main`  
**Commit observado:** `940b8622db99b4f6feb4321a5bb895fc26fd144a`

Esta revisão foi feita sobre os ficheiros remotos do repositório e o estado disponível das integrações. Não representa validação num dispositivo físico, auditoria de segurança independente, nem aprovação para publicação.

## Estado observado

- A app Android usa Kotlin/Compose, Google Fused Location Provider, `minSdk 26` e `targetSdk 36`.
- O manifesto declara permissões de localização aproximada e precisa, permissões de foreground service de localização e notificações. O serviço `NavigationForegroundService` não é exportado.
- A navegação usa um foreground service com notificação persistente, actualizações de localização de alta precisão com intervalo pretendido de 1 s, intervalo mínimo de 500 ms e distância mínima de 2 m.
- O serviço rejeita coordenadas não finitas, fixes fora de ordem, fixes marcados como simulados e precisão pior que 100 m. O limite de 100 m é uma regra operacional da app; não é uma garantia de integridade do sinal GNSS.
- A localização inicial usa `getCurrentLocation()`, com idade máxima de fix de 3 s e duração limitada a 5 s.
- O mapa local é carregado por `WebViewAssetLoader` em origem HTTPS. Acesso a ficheiros/conteúdos e mixed content estão desactivados; JavaScript continua activo e há uma ponte JavaScript para TTS.
- O workflow Android compila apenas `assembleDebug` e publica o APK debug como artefacto. As actions estão referenciadas por tags e o workflow não declara permissões mínimas de `GITHUB_TOKEN`.
- O workflow backend usa Node 20, `npm install` e `npm run check`; ainda não comprova testes de endpoint, integração, persistência ou build do deployment Vercel.
- O projecto Vercel `gps` existe. A listagem mostrou deployments de produção em estado `READY` e anteriores em `ERROR`. O estado `READY` confirma o deployment, mas não confirma comportamento funcional da API nem integração Android.
- O protótipo Canva “Protótipo mobile MotoGPS” existe e foi localizado; o conteúdo visual não foi recuperável pela ligação actual da ferramenta nesta revisão.

## Achados prioritários

### P0 — comportamento após recusa da permissão de notificações

Em `MainActivity`, o callback de `POST_NOTIFICATIONS` inicia o serviço sempre que `pendingStartNavigation` está activo, sem consultar o resultado de concessão. No Android 13+, a recusa não impede necessariamente iniciar um foreground service, mas a notificação não aparece na gaveta normal. Para uma navegação que promete notificação persistente e controlo de paragem, a app deve tratar a recusa explicitamente: explicar a limitação, permitir ao utilizador decidir se continua e não apresentar a experiência como se a notificação estivesse visível. Validar também o canal de notificação desactivado nas definições do sistema.

### P1 — integridade GPS não equivale a anti-falsificação

`Location.isMock` detecta posições assinaladas pelo fornecedor Android como simuladas. Não prova que o sinal GNSS é autêntico e pode não detectar spoofing externo ou adulteração do dispositivo. Usar o estado apenas como sinal de risco/diagnóstico; não tratar a ausência de `isMock` como prova de localização verdadeira. Para decisões com consequências, combinar idade, precisão, velocidade/aceleração plausíveis e continuidade da rota, e mostrar degradação/alerta em vez de afirmar segurança garantida.

### P1 — segurança da ponte WebView e navegação

A ponte `AndroidVoice` é aceitável apenas enquanto o documento e todos os scripts executados forem conteúdo controlado pela app. A página do mapa também carrega recursos/serviços externos; fixar ou rever dependências e impedir que navegação para origens externas passe a executar com a ponte activa. Validar os esquemas/hosts aceites, abrir links externos no browser do sistema, manter depuração WebView desligada em release e rever CSP/recursos remotos. A configuração de ficheiros e mixed content já está bem endurecida.

### P1 — permissão de localização aproximada

Android pode conceder apenas localização aproximada mesmo quando a app solicita precisa. O código aceita qualquer uma; a app deve comunicar que a navegação pode ficar menos exacta, evitar indicar velocidade/direcção enganadoras e validar visualmente o comportamento com permissão aproximada e precisa.

### P1 — CI e cadeia de fornecimento

Declarar explicitamente `permissions: contents: read` nos workflows que só fazem checkout/build; fixar actions de terceiros por SHA completo (ou documentar a política de confiança/actualização); preferir `npm ci` com lockfile; adicionar build de release não assinado e verificação de artefacto separado do processo de assinatura. Não guardar chaves de assinatura no repositório. Não criar publicação automática.

### P1 — Vercel e backend

Verificar, num deployment preview, os endpoints de health, geocoding e routing; validar parâmetros, limites, timeouts, respostas de erro e uso responsável dos serviços públicos Nominatim/OSRM. O deployment READY não substitui estes checks. Confirmar também que o projecto aponta para a raiz/configuração pretendida, que segredos só existem no ambiente necessário e que dados pessoais de localização não são registados em logs.

## Plano antes de release

1. Corrigir a decisão do callback de notificação e desenhar o fallback explícito quando notificações/canal estão desligados.
2. Testar em Android 13–16: permissão precisa/aproximada/negada, localização desligada, notificação permitida/negada, canal desligado, app em background, ecrã bloqueado, processo morto, reinício do serviço e acção “Parar”.
3. Testar GPS em campo: primeiro fix, túneis/perda de sinal, precisão degradada, saltos, velocidade, bearing e consumo de bateria. Não aceitar apenas simulação/emulador como validação de navegação.
4. Testar WebView com rede indisponível, falhas de tile/geocoding/routing, conteúdo externo inesperado, links e recriação/destruição da Activity.
5. Executar workflow Android para debug e release; adicionar testes automatizados adequados. Fazer teste manual num telemóvel real antes de aprovar qualquer AAB.
6. Validar endpoints e configuração Vercel num preview isolado; rever logs, headers, rate limits e dados enviados a terceiros.
7. Rever política de privacidade, declaração de dados do Google Play, justificações de localização em background/foreground e requisitos de conta de developer antes de preparar submissão.

## Decisão

**Estado: não pronto para publicação.** A base contém melhorias reais para GPS e WebView e já existe deployment Vercel READY. Continuam por resolver/validar a recusa de notificações, comportamento em dispositivos reais, tratamento da incerteza da localização, revisão final da ponte WebView, endurecimento CI, validação funcional do backend e a documentação de privacidade/release.

## Referências oficiais

- [Pedido de actualizações de localização Android](https://developer.android.com/develop/sensors-and-location/location/request-updates)
- [Foreground services de localização](https://developer.android.com/develop/background-work/services/foreground-services#location)
- [Permissões de localização Android](https://developer.android.com/develop/sensors-and-location/location/permissions)
- [Acesso a conteúdo local em WebView / WebViewAssetLoader](https://developer.android.com/develop/ui/views/layout/webapps/load-local-content)
- [Segurança de WebView](https://developer.android.com/privacy-and-security/risks/webview-unsafe-file-inclusion)
- [Requisito Google Play de target API](https://developer.android.com/google/play/requirements/target-sdk) — a partir de 31 de Agosto de 2026, novas apps e actualizações devem visar API 36 ou superior.
- [Uso seguro do GitHub Actions](https://docs.github.com/en/actions/reference/security/secure-use)
- [Permissões mínimas para GITHUB_TOKEN](https://docs.github.com/en/actions/security-for-github-actions/security-guides/automatic-token-authentication)
- [Variáveis de ambiente Vercel](https://vercel.com/docs/environment-variables)
- [Deployments preview Vercel](https://vercel.com/docs/deployments/environments)
