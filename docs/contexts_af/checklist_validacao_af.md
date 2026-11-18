---
titulo: Checklist de validação final da AF (código)
uso: conferir item por item quando todos os PRs estiverem mergeados na branch af, antes de gravar o vídeo
base: docs/contexts_af/planejamento_af.md (as seções citadas são desse plano)
---

# Checklist de validação final da AF

Este checklist serve para validar o **código e o funcionamento**, quando todos os PRs já estiverem na branch `af`. O roteiro do vídeo fica para depois.

Como usar: rodar tudo a partir de um **clone limpo da branch `af`**, marcar `[x]` no que passou, e anotar ao lado o que falhou. Os detalhes que mais tendem a ser esquecidos estão marcados com ⚠️.

---

## 0. Preparação

- [ ] Clone limpo da `af` (não usar a pasta de trabalho de ninguém, que pode ter arquivo local que não foi commitado).
- [ ] `.env` criado a partir do `.env.example`, com a chave do LLM (seção 5.6 do plano).
- [ ] Volumes antigos apagados, para testar do zero:
  ```
  docker compose down -v
  ```

---

## 1. Git e branches

- [ ] ⚠️ A `main` continua exatamente no commit da AC2 (`40226a2`), sem nenhum commit novo.
  ```
  git log origin/main -1 --oneline
  ```
- [ ] Todos os PRs foram abertos com **base `af`** (nenhum PR mergeado na `main`).
- [ ] Existe pelo menos 1 PR mergeado de cada integrante, cada um commitado com a **conta da própria pessoa**.
- [ ] Nenhum PR foi mergeado pelo próprio autor (quem aprovou e mergeou foi o tech lead).
- [ ] Mensagens de commit em inglês, com prefixo (`feat`, `fix`, `docs`...).
- [ ] ⚠️ O `.env` **não** está no repositório, e nenhuma chave de API aparece no histórico.
  ```
  git log --all -p -S "sk-" --oneline
  ```
- [ ] O `.gitignore` tem `.env`, e o `.env.example` está commitado (sem a chave).

---

## 2. Docker e infraestrutura (vale para todos)

- [ ] ⚠️ `docker compose up --build` sobe **tudo**, sem nenhum outro comando antes ou depois.
- [ ] Todos os containers ficam `healthy` ou `running`, sem nenhum reiniciando em loop.
  ```
  docker compose ps
  ```
- [ ] ⚠️ Só o gateway (8080) publica porta de API no host. O Core, o ai-service e o ml-service **não** aparecem com porta no `docker compose ps` (seção 5.2).
- [ ] Portas publicadas: front 5173, gateway 8080, painel do RabbitMQ 15672, pgAdmin 5050, Postgres 5432.
- [ ] Todo serviço novo tem `healthcheck`, e quem depende dele usa `depends_on: condition: service_healthy` (o Core espera o Postgres e o RabbitMQ, o gateway espera o Core).
- [ ] ⚠️ O `.dockerignore` da raiz ignora `gateway/`, `ai-service/`, `ml-service/`, `loadtest/`, `frontend/`, `docs/` e `evidencias/`.
- [ ] Nenhum `docker-compose.override.yml` foi commitado (ele está no `.gitignore` e é só para testes locais).
- [ ] O Core roda na porta 8081 dentro do docker (`application-docker.properties`).
- [ ] ⚠️ **Sem chave de LLM** (apagar o `.env` ou deixar `OPENAI_API_KEY` vazio), a stack inteira sobe mesmo assim, e só as rotas de IA respondem `503` com mensagem clara (seção 5.6).

---

## 3. Arquitetura (vale para todos)

- [ ] O build do Core passa com todos os testes.
  ```
  ./mvnw clean verify
  ```
- [ ] ArchUnit com as 3 regras (seção 7.2):
  - [ ] nenhum módulo acessa o `internal` de outro módulo;
  - [ ] `..api..` não depende de `..internal..`;
  - [ ] `..domain..` não importa `org.springframework..` nem `jakarta..`.
- [ ] ⚠️ O ArchUnit **falha de verdade** quando se força um import proibido. Testar uma vez e desfazer o import (é a prova do experimento de manutenibilidade).
- [ ] O Core tem 6 módulos com `api/` + `internal/`: `cursos`, `catalogo`, `colaboradores`, `ranking`, `competencias`, `treinamentos`.
- [ ] ⚠️ `cursos/api/AlunoResponse.java` **não** importa mais nada de `cursos.internal` (era o ponto do feedback da AC2).
- [ ] Contratos entre módulos usados de verdade (seção 7.2 e seção 9.4):
  - [ ] `cursos` usa `catalogo.api.CatalogoApi` (valida que o curso existe antes de aplicar a US1);
  - [ ] `ranking` usa `cursos.api.CursosApi` (bônus do vencedor);
  - [ ] `competencias` usa `colaboradores.api.ColaboradoresApi` (perfil para o ML).
- [ ] Hexagonal no ai-service e no ml-service: pastas `domain/`, `application/` e `adapters/` (seção 4.3), e o domínio sem import de framework.
- [ ] Toda integração externa fica atrás de uma porta (interface): `ClassificadorPort`, `PublicadorTreinamentoPort`, `BaseConhecimento`, `ModeloLinguagem`, `CoreGateway`.
- [ ] Os testes de domínio da AC1 (`AlunoTest`) continuam passando, e a cobertura do `domain/` continua em 100% no JaCoCo.

---

## 4. Bruno: RNF03 (gateway, JWT e acesso)

- [ ] Login com os 3 usuários de demo funciona (`admin`, `ana`, `carlos`, seção 5.3) e devolve `token`, `roles` e `colaboradorId`.
- [ ] Senha errada dá `401`.
- [ ] Chamada a `/api/**` sem token dá `401`.
- [ ] Token expirado ou adulterado dá `401`.
- [ ] `ana` (COLABORADOR) tentando `POST /api/cursos` dá `403`.
- [ ] Rotas só de ADMIN bloqueadas para COLABORADOR: `POST /api/cursos`, `POST /api/colaboradores`, `GET /api/colaboradores` (listar todos), `POST /api/treinamentos`, `POST /api/ranking/fechamentos/**`.
- [ ] ⚠️ **Anti-falsificação:** um cliente mandando o header `X-User-Roles: ADMIN` com o token da `ana` continua sendo tratado como COLABORADOR. O gateway sobrescreve os headers `X-User-*` e `X-Colaborador-Id`.
- [ ] As rotas chegam ao serviço certo: `/api/ia/**` vai para o ai-service, e o resto de `/api/**` vai para o Core. A rota de IA está declarada **antes** da genérica.
- [ ] ⚠️ CORS configurado **só** no gateway. O `CorsConfig` do Core foi removido (CORS nos dois gera header duplicado, e o navegador bloqueia).
- [ ] `JWT_SECRET` vem de variável de ambiente, com um default só para desenvolvimento.
- [ ] Senhas dos usuários em memória guardadas com BCrypt.
- [ ] Testes do gateway com `WebTestClient` passando (401, 403 e sobrescrita de header).
- [ ] Front: tela de login, token guardado em `sessionStorage`, logout automático quando a API devolve `401`, e navegação por abas.
- [ ] O front carrega os arquivos `js/colaboradores.js`, `js/ia.js`, `js/ml.js` e `js/ranking.js` (um por aba).

## 5. Bruno: RF01 (colaboradores e cursos)

- [ ] O seed cria uns 10 cursos e os colaboradores 1 (Ana) e 2 (Carlos), e não duplica ao reiniciar o container.
- [ ] ⚠️ Os ids do seed batem com os usuários do gateway (`ana` = colaborador 1, `carlos` = colaborador 2).
- [ ] `GET /api/cursos` lista, e `GET /api/cursos?area=DEVOPS` filtra por área.
- [ ] `GET /api/cursos/{id}` inexistente dá `404` no formato `{"erro": "..."}`.
- [ ] ADMIN cadastra curso e colaborador (`201`).
- [ ] `ana` vê o próprio perfil (`GET /api/colaboradores/1`) e recebe `403` no perfil do Carlos (`/2`).
- [ ] Email mascarado (`a***@empresa.com`) para quem não é ADMIN, e completo para o ADMIN.
- [ ] Nenhum CPF ou dado pessoal desnecessário guardado.
- [ ] Os enums `Area` e `Nivel` batem com a seção 5.4 (o `Nivel` é o mesmo que o ML devolve).
- [ ] Concluir na US1 um curso que não existe no catálogo é rejeitado, porque o `cursos` valida pelo `CatalogoApi`.
- [ ] Rotas aparecem no Swagger do Core.
- [ ] Aba "Colaboradores e Cursos" no front funcionando (o formulário de cadastro só aparece para o admin).

---

## 6. João: RF03 (RAG)

- [ ] Existem de 4 a 6 documentos institucionais em `ai-service/src/main/resources/docs/`.
- [ ] ⚠️ Os títulos de curso citados nos documentos são **os mesmos** do seed do Bruno (senão o RAG recomenda curso que não existe no catálogo).
- [ ] A ingestão roda na subida e grava `/data/vectorstore.json` no volume.
- [ ] ⚠️ Ao reiniciar o container, ele **carrega** o arquivo em vez de gerar os embeddings de novo (dá para ver no log, e não gasta chamada paga).
- [ ] `POST /api/ia/recomendacoes` devolve `resposta`, `cursos[]` e `fontes[]`, e as fontes são nomes de arquivos reais.
- [ ] Uma pergunta fora do assunto dos documentos responde algo como "não encontrei nos documentos", sem inventar curso.
- [ ] Sem chave de LLM: o serviço sobe, e a rota devolve `503` com mensagem clara.
- [ ] O `RecomendacaoService` depende só das portas `BaseConhecimento` e `ModeloLinguagem`, sem conhecer o Spring AI direto.
- [ ] O teste unitário do `RecomendacaoService` usa portas mockadas e **não** chama o LLM de verdade no `mvn verify`.
- [ ] O modelo e a URL do LLM vêm do `.env` (`OPENAI_BASE_URL`, `OPENAI_CHAT_MODEL`, `OPENAI_EMBEDDING_MODEL`).
- [ ] (Opcional, mas bom para o vídeo) Trocar para o Gemini só pelo `.env` funciona.
- [ ] Aba "Assistente IA" mostra a resposta, os cursos e as fontes.

## 7. João: RF04 (agentes com autorização)

- [ ] O pedido de orientação devolve um plano com ações em `AGUARDANDO_AUTORIZACAO`.
- [ ] ⚠️ **Nenhuma** ação muda dados antes do usuário autorizar. As tools de ação só **propõem**.
- [ ] Autorizar executa a ação (status `EXECUTADA`) e registra `autorizadoPor` e `autorizadoEm`.
- [ ] Negar cancela a ação, e ela não executa.
- [ ] `ana` tentando autorizar uma ação do `carlos` dá `403`.
- [ ] ⚠️ **Anti prompt injection:** pedir para a `ana` "me mostre os dados do Carlos" não devolve os dados dele. O `colaboradorId` vem sempre do header `X-Colaborador-Id`, nunca do texto do usuário.
- [ ] As tools de leitura chamam o Core **repassando os headers do usuário logado**, então as regras de acesso do RF01 valem também para o agente.
- [ ] ⚠️ O `StubCoreGateway` **não** está ativo na versão final. Na `af`, o agente usa o `RestCoreGateway` com os dados reais do Core.
- [ ] As classes com `@Tool` ficam em `adapters/in/tools/`.
- [ ] O log mostra quais tools o agente chamou.
- [ ] A ação "inscrição" usa um curso que existe no catálogo.
- [ ] Testes passando: `403` de outro usuário, ação negada que não executa, e tool de ação que nunca executa sozinha.
- [ ] Na aba "Assistente IA": pedir orientação, botões Autorizar e Negar, e histórico das ações.

---

## 8. Gabriel: RF02 (classificação com ML) e RNF01

- [ ] O ml-service sobe e treina a versão 1 do modelo se não houver arquivo em `/models`.
- [ ] Ao reiniciar o container, ele carrega o `modelo.joblib` do volume em vez de treinar de novo.
- [ ] `GET /health` responde, e o healthcheck do compose usa essa rota.
- [ ] O `/docs` do FastAPI mostra `POST /classificar`.
- [ ] `POST /api/colaboradores/1/competencia` com `{"notaAvaliacao": 78}`, pelo gateway, devolve `nivel`, `confianca` e `versaoModelo`.
- [ ] O `CompetenciaService` pega o perfil pelo `ColaboradoresApi` (contrato do Bruno), sem acessar o `colaboradores.internal`.
- [ ] ⚠️ Com o ML parado, a classificação devolve `503` com mensagem clara, e o resto do Core (cadastro, consulta e ranking) **continua funcionando** (RNF04).
  ```
  docker compose stop ml-service
  ```
- [ ] Timeout de 1,5 s no `RestClient` do Core para o ML.
- [ ] O uvicorn roda com `--workers 2` (ou mais).
- [ ] `pytest` passa no ml-service, com o classificador fake no teste de caso de uso.
- [ ] Teste Java do `CompetenciaService` com `ClassificadorPort` mockado, incluindo o caso do `503`.
- [ ] ⚠️ k6 da classificação (`loadtest/classificacao.js`): 300 VUs, pelo **gateway**, com os thresholds `p(95)<2000` e `http_req_failed rate<0.01` **passando**.
  ```
  docker run --rm -i --network host grafana/k6 run - < loadtest/classificacao.js
  ```
- [ ] Resultado do k6 salvo (print e `loadtest/resultado.json`) e registrado no README e no ADR-AF.
- [ ] Aba "Competências e ML" mostrando o nível e a confiança.

## 9. Gabriel: RF05 (treinamento via RabbitMQ) e RNF02

- [ ] O painel do RabbitMQ abre em `http://localhost:15672` e mostra a exchange `ml.treinamento`, as filas `ml.treinamento.solicitado` e `ml.treinamento.status`, e a DLQ `ml.treinamento.solicitado.dlq` (seção 5.5).
- [ ] As filas são `durable`, e as mensagens são `persistent`.
- [ ] O Core e o ml-service declaram as filas na subida, então tanto faz qual dos dois sobe primeiro.
- [ ] `POST /api/treinamentos` (admin) responde **`202`**, imediatamente, com status `PENDENTE`.
- [ ] O status passa por `EM_ANDAMENTO` e chega em `CONCLUIDO`, com `acuracia` e `versaoModelo` novas.
- [ ] Depois do treino, o `/classificar` usa a versão nova **sem reiniciar** o container (a `versaoModelo` na resposta sobe).
- [ ] ⚠️ **Experimento do RNF02:**
  - [ ] Parar o ML.
    ```
    docker compose stop ml-service
    ```
  - [ ] Pedir 2 treinamentos pelo front. Os dois respondem na hora e ficam `PENDENTE`.
  - [ ] As 2 mensagens aparecem prontas na fila do painel do RabbitMQ.
  - [ ] Subir o ML de novo.
    ```
    docker compose start ml-service
    ```
  - [ ] Os dois treinamentos chegam a `CONCLUIDO`, e nenhuma mensagem se perde.
- [ ] Ack manual só depois de terminar o treino, com `prefetch_count=1`.
- [ ] Um treino que falha publica `FALHOU`, e a mensagem vai para a DLQ (não fica em loop de reentrega).
- [ ] O ml-service não cai se o RabbitMQ demorar para subir: tenta conectar de novo a cada 5 s.
- [ ] Testes: no Core, o `POST` responde `202` e publica a mensagem (`RabbitTemplate` mockado), e o listener atualiza o status. No Python, o caso de uso `treinar` com fakes.
- [ ] Na aba do front, a tabela de solicitações atualiza o status sozinha (polling de 3 s), e o botão de solicitar só aparece para o admin.

## 10. Gabriel: Ranking (feedback da AC2)

- [ ] Os cenários BDD do ranking estão no README, no mesmo formato da AC1 (mais pontos vence, empate, mês fechado duas vezes).
- [ ] `domain/RankingMensal.java` sem Spring, com testes de domínio passando e cobertura de 100% no JaCoCo.
- [ ] Prints do ciclo RED, GREEN e REFACTOR do ranking, como na AC1.
- [ ] Pontuação em constantes com nome de negócio (tópico = 2, comentário = 1).
- [ ] Regra de empate implementada e testada: mais comentários vence e, persistindo o empate, a primeira participação mais cedo.
- [ ] `Aluno.bonificar(int)` no domínio e na `AlunoEntity`, com teste.
- [ ] `CursosApi.bonificar(...)` existe em `cursos/api`, e o `RankingService` usa **só** esse contrato (o ArchUnit garante).
- [ ] ⚠️ O PR do ranking **não alterou** nada em `cursos/internal` além da implementação do `CursosApi` (conferir o diff, é a prova de manutenibilidade).
- [ ] `GET /api/ranking?mes=AAAA-MM&top=10` devolve as posições ordenadas.
- [ ] Fechar o mês dá +1 curso liberado ao vencedor, uma única vez.
- [ ] Fechar o mesmo mês de novo dá `409`, sem bonificar outra vez (constraint única em `mes`, mais a checagem no serviço).
- [ ] A consulta agrega no banco (`GROUP BY`), com índice em `(registradaEm, alunoId)`, e não carrega todas as participações em memória.
- [ ] O seed tem participações no mês atual, então o ranking não aparece vazio.
- [ ] k6 do ranking (`loadtest/ranking.js`) rodado, com o resultado registrado (seção 9.5).
- [ ] Aba "Ranking": tabela, registrar tópico e comentário, e "Fechar mês" só para o admin.

---

## 11. Experimentos dos atributos de qualidade (feedback da AC2)

Cada resultado precisa estar **preenchido na tabela "Validação" do ADR-AF** (hoje está "a medir") e no README.

- [ ] Manutenibilidade: ArchUnit passando, mais o print dele falhando com um import proibido.
- [ ] Escalabilidade do ranking: k6 com p95 e taxa de erro. Se der tempo, comparar com e sem o índice.
- [ ] Complexidade operacional: `docker stats --no-stream` com a stack inteira, mais a contagem de containers da Alternativa A x Alternativa B.
- [ ] RNF01: k6 da classificação com p95 < 2 s.
- [ ] RNF02: experimento da seção 9 deste checklist, com prints.
- [ ] Prints salvos em `evidencias/af/<requisito>/`.

---

## 12. Documentação ligada ao código

- [ ] O README da `af` diz no topo que a `main` guarda a AC2.
- [ ] O README tem os C4 de nível 1, 2 e 3, com legenda "implementado / planejado" (feedback da AC2).
- [ ] O conteúdo antigo do README (AC1) foi movido para `docs/ac1-atdd.md`, com link no README.
- [ ] O README tem uma seção por requisito, escrita pelo dono.
- [ ] O README tem a tabela "Atendimento ao feedback da AC2", com link para o código de cada item.
- [ ] O README registra as versões usadas no gateway e no ai-service (Spring Boot, Spring Cloud, Spring AI).
- [ ] O README tem "Como rodar": `.env` opcional, `docker compose up --build`, URLs e usuários de demo.
- [ ] O ADR-AF continua coerente com o que foi implementado (se algo mudou durante o desenvolvimento, atualizar o texto).
- [ ] As Limitações do README batem com a seção 12 do plano e com o que de fato ficou faltando.

---

## 13. Validação final, de ponta a ponta

Um passeio completo pelo front, logado como `ana` e depois como `admin`, sem abrir Postman:

- [ ] `ana`: login, ver cursos, ver o próprio perfil, fazer a avaliação de competência, pedir recomendação, pedir orientação, autorizar uma ação, registrar participação no ranking.
- [ ] `admin`: login, cadastrar curso, solicitar treinamento e acompanhar até concluir, fechar o mês do ranking e ver o vencedor com +1 curso.
- [ ] Nenhum erro no console do navegador, e nenhum container reiniciando durante o passeio.
