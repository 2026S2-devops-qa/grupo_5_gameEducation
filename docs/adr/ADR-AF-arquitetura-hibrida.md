# ADR-AF: Arquitetura híbrida para a plataforma de educação corporativa inteligente

- **Origem:** Avaliação Final (AF), Estudo de Caso 1, item e) "Registro da decisão arquitetural"
- **Data:** 2026-11-18
- **Status:** aceita (implementação em andamento na branch `af`)
- **Relacionado:** [ADR-001](ADR-001-monolito-modular.md) (monólito modular, Desafio 1)

## Contexto

A empresa de educação corporativa quer uma plataforma em que colaboradores façam avaliações de competências, recebam recomendações personalizadas de cursos e acompanhem sua evolução. O case pede Spring Boot para o negócio, Python para ML, RabbitMQ para mensageria, REST entre aplicações, RAG sobre documentos institucionais e agentes de IA.

| ID    | Requisito                                                                            |
| ----- | ------------------------------------------------------------------------------------ |
| RF01  | Cadastro e consulta de colaboradores e cursos                                        |
| RF02  | Classificar o nível de competência do colaborador com ML                             |
| RF03  | Recomendar cursos com RAG sobre documentos institucionais                            |
| RF04  | Agentes de IA coordenando orientação e acompanhamento, com autorização do usuário    |
| RF05  | Acompanhar o andamento das solicitações de treinamento de modelos                    |
| RNF01 | 95% das classificações em até 2 s, com 300 usuários simultâneos                      |
| RNF02 | Aceitar solicitações de treinamento com o ML temporariamente indisponível            |
| RNF03 | Proteger dados pessoais e controlar o acesso dos agentes a informações e ferramentas |
| RNF04 | Evoluir os componentes de IA sem comprometer cadastro e consulta                     |
| RNF05 | Custos de infraestrutura e operação compatíveis com uma equipe reduzida              |

Restrições e premissas:

- Equipe real de 3 integrantes, sem experiência prévia em operar sistemas distribuídos.
- O Core já existe em Java/Spring Boot, organizado como monólito modular (ADR-001), com o `domain/` da AC1 coberto por TDD e livre de framework.
- O ML precisa ser em Python, então pelo menos um processo separado do Core é inevitável.
- Tudo deve subir com um único comando: `docker compose up --build`.

## Alternativas consideradas

| Alternativa                                 | Benefício                                                                                                                        | Custo / risco                                                                                                                                            |
| ------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **A. Monólito modular híbrido** (escolhida) | Core de negócio em um único deploy e um único banco. Só a IA (ML, RAG, agentes) fica em serviços próprios. Reaproveita o ADR-001 | Exige disciplina nas fronteiras entre módulos do Core. Os módulos do Core escalam juntos                                                                 |
| B. Microsserviços completos                 | Cada capacidade (cursos, colaboradores, ranking, competências, treinamentos, IA) com deploy e escala independentes               | Uns 6 serviços e 6 bancos para 3 pessoas operarem. Consistência distribuída, observabilidade e mais pipelines, sem métrica que justifique (fere o RNF05) |
| C. Tudo no Core, inclusive a IA             | Um processo só, operação mínima                                                                                                  | Impossível para o ML em Python. Uma falha ou deploy da IA derruba cadastro e consulta (fere o RNF04)                                                     |

## Decisão

Adotar a **Alternativa A, monólito modular híbrido**:

- **API Gateway** (Spring Cloud Gateway) como única porta de entrada. Valida o JWT, aplica as regras de acesso e repassa a identidade aos serviços em headers.
- **Core** (Spring Boot) continua monólito modular, conforme o ADR-001, agora com os módulos `cursos`, `catalogo`, `colaboradores`, `ranking`, `competencias` e `treinamentos`, que conversam só pelos contratos públicos em `<modulo>/api/`.
- **ai-service** (Spring Boot + Spring AI) separado, com RAG (RF03) e agentes (RF04).
- **ml-service** (Python + FastAPI + scikit-learn) separado, com a classificação (RF02) e o treinamento (RF05).
- **RabbitMQ** entre o Core e o ml-service para o treinamento.

## Justificativa

- **RNF04:** IA e ML em processos próprios evoluem, sobem e caem sem afetar o cadastro e a consulta do Core.
- **RNF05:** só é separado o que tem motivo (linguagem diferente ou ciclo de evolução diferente). O negócio continua em um deploy e um banco.
- **RNF02:** a fila do RabbitMQ guarda os pedidos de treino enquanto o ML está fora.
- **RNF03:** o gateway centraliza a autenticação, e os serviços internos não publicam porta no host.
- **Continuidade:** mantém a decisão do ADR-001, em vez de recomeçar a arquitetura.

## ASRs (requisitos arquiteturalmente significativos)

| ASR                                            | Como influencia a arquitetura                                                                                                                                                                                                                |
| ---------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **RNF04**: evolução independente da IA         | **Organização:** a IA sai do Core e vira serviços próprios (ai-service e ml-service), com deploy e falha isolados                                                                                                                            |
| **RNF02**: aceitar treino com o ML fora        | **Integração:** o treinamento usa RabbitMQ (fila durável, mensagem persistente, DLQ) em vez de REST, e o Core responde `202 Accepted` sem esperar o ML                                                                                       |
| **RNF03**: dados pessoais e acesso dos agentes | **Segurança:** gateway com JWT como única entrada, identidade repassada por headers que o gateway sobrescreve, serviços internos sem porta publicada, e tools dos agentes que agem só com a identidade do usuário e só após autorização dele |

## Comunicação: REST ou RabbitMQ

| Interação                                  | Tipo     | Motivo                                                                                           |
| ------------------------------------------ | -------- | ------------------------------------------------------------------------------------------------ |
| Front → Gateway → Core (RF01)              | REST     | resposta imediata para a tela, operação rápida                                                   |
| Core → ml-service: classificar (RF02)      | REST     | precisa de resposta imediata (RNF01). Inferência com o modelo em memória leva milissegundos      |
| Front → Gateway → ai-service (RF03, RF04)  | REST     | o usuário aguarda a recomendação ou o plano na tela                                              |
| ai-service → Core: tools dos agentes       | REST     | o agente precisa do dado para continuar o raciocínio                                             |
| Core → ml-service: solicitar treino (RF05) | RabbitMQ | processamento longo, não pode travar a requisição, e precisa sobreviver ao ML fora do ar (RNF02) |
| ml-service → Core: status do treino (RF05) | RabbitMQ | o ML publica o progresso sem conhecer o endereço do Core (desacoplamento)                        |

Falhas: no REST, timeout curto, e `503` com mensagem clara quando o serviço de destino está fora, sem derrubar o resto. No RabbitMQ, ack manual só depois de processar e DLQ para mensagens que falham.

## Decisão complementar: arquitetura hexagonal

Definida por Gabriel Ferreira. Os serviços com regra de negócio (Core, ai-service e ml-service) seguem **hexagonal (portas e adaptadores)**, e não clean architecture.

- **Por quê:** o sistema é feito de integrações externas (LLM, vector store, ML, RabbitMQ), e trocar ou isolar essas tecnologias é o que a hexagonal resolve. A clean exigiria 4 camadas e várias classes por operação, o que pesa para uma equipe de 3 (RNF05). E o `domain/` do Core já é framework-free desde a AC1, então a hexagonal formaliza o que já existe sem quebrar a estrutura `api/` + `internal/` do ADR-001.
- **Como:** o domínio não importa framework. Cada integração externa fica atrás de uma porta (interface) implementada por um adaptador. Os contratos `<modulo>/api/*Api` do Core são portas de entrada entre módulos. Um teste ArchUnit barra imports de Spring/Jakarta no `domain/`.
- **Exceção:** o gateway não tem regra de negócio, então não aplica hexagonal.

## Consequências e trade-offs

- **Ganha:** IA isolada (RNF04), resiliência no treinamento (RNF02), segurança centralizada (RNF03), e o Core continua simples de operar.
- **Paga:** mais containers que no ADR-001 (gateway, ai-service, ml-service, RabbitMQ), latência de rede entre Core e ML na classificação, e consistência eventual no status do treinamento.
- **Trade-off aceito:** os módulos do Core escalam juntos. Se algum (por exemplo o ranking) mostrar gargalo medido, ele é extraído, e isso fica barato porque já está isolado (Alternativa C do ADR-001).
- **Trade-off da hexagonal:** a hexagonal não define a organização interna do núcleo, e isso é compensado por uma estrutura de pastas fixa por serviço e pelo ArchUnit. O JPA não ganha porta própria, porque o `JpaRepository` já é uma interface.

## Validação

Os resultados entram aqui conforme os experimentos forem rodados (plano em `docs/contexts_af/planejamento_af.md`, seção 9.5).

| Atributo                            | Experimento                                                               | Resultado |
| ----------------------------------- | ------------------------------------------------------------------------- | --------- |
| Manutenibilidade (ADR-001)          | ArchUnit nas fronteiras entre módulos e no `domain/`                      | a medir   |
| Escalabilidade do ranking (ADR-001) | k6 no `GET /api/ranking` pelo gateway                                     | a medir   |
| Complexidade operacional (ADR-001)  | `docker stats` e contagem de containers (A x B)                           | a medir   |
| RNF01                               | k6 com 300 VUs na classificação, p95 < 2 s                                | a medir   |
| RNF02                               | parar o ml-service, solicitar treinos, religar e ver os status concluírem | a medir   |

## Gatilho de revisão

Extrair algum módulo do Core (ranking, colaboradores, catálogo) para um serviço próprio só quando houver métrica de carga, ou time e frequência de deploy, que justifique o custo operacional. Até lá, vale este ADR.
