# Registro Arquitetural - Gamificação para Educação Continuada

Registro cumulativo das decisões arquiteturais do Architecture Lab (Exercício B1), ou seja, cresce a cada desafio resolvido.

## Equipe

| Integrante                     | RA     |
| ------------------------------ | ------ |
| Bruno da Silveira Escanhoela   | 236793 |
| Gabriel Ferreira do Nascimento | 236085 |
| João Guilherme Volta Kinol     | 235255 |

## Contexto geral

Na AC1, a equipe construiu o MVP de Gamificação para Educação Continuada: Core em Java/Spring Boot, banco relacional (H2 em dev/teste, PostgreSQL em produção/Docker), com a US1 (liberação automática de 3 cursos ao concluir um curso com média acima de 7,0) implementada, testada por TDD e com 100% de cobertura no domínio.

No B1, esse mesmo produto passa a receber crescimento de usuários, novas capacidades de IA, eventos, IoT e exigências de operação. A equipe assume o papel de Tech Lead: antes de escolher tecnologia ou alterar código, cada desafio exige justificar a decisão arquitetural a partir dos requisitos e das evidências do sistema, seguindo o padrão RF → RNF → ASR → RPC → Alternativas → ADR → C4 → Evidência.

Nota importante sobre o estado real do projeto: o case descreve um cenário com 1.000 a 100.000 usuários e uma equipe de 6 desenvolvedores. A equipe real tem 3 integrantes, e só a US1 está implementada em código (ranking e moedas/badges/Premium ainda são apenas BDD). Essa diferença é tratada, ao longo dos desafios, como contexto que reforça as decisões de menor risco, não como algo a ignorar.

## Status dos desafios

| #   | Desafio                     | Status    | ADR                                           |
| --- | --------------------------- | --------- | --------------------------------------------- |
| 1   | Drivers e Evolução do Core  | Concluído | [ADR-001](../adr/ADR-001-monolito-modular.md) |
| 2   | Organização Interna do Core | não realizado (substituído pela AF) | -                                             |
| 3   | Serviço Python e IA         | não realizado (substituído pela AF) | -                                             |
| 4   | Comunicação e Eventos       | não realizado (substituído pela AF) | -                                             |
| 5   | IoT e MQTT                  | não realizado (substituído pela AF) | -                                             |
| 6   | CI/CD e Observabilidade     | não realizado (substituído pela AF) | -                                             |
| AF  | Avaliação Final: plataforma de educação corporativa inteligente | Decidido, implementação em andamento | [ADR-AF](../adr/ADR-AF-arquitetura-hibrida.md) |

Só o Desafio 1 do Architecture Lab foi realizado. Os desafios 2 a 6 não foram feitos: a disciplina passou para a Avaliação Final, cujo estudo de caso já cobre parte dos temas previstos para eles (organização interna do Core, serviço Python e IA, comunicação e eventos). Essas decisões estão registradas no ADR-AF, e não em ADRs numerados por desafio.

## Matriz cumulativa de ASRs

| Desafio | ASR                      | Por que virou ASR                                                                                                                                      | Decisão resultante                                                                              |
| ------- | ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------- |
| 1       | Escalabilidade           | Ranking projetado para crescer desproporcionalmente em relação às demais funcionalidades                                                               | Monólito Modular, isolando o módulo de ranking para uma extração futura de baixo custo          |
| 1       | Complexidade operacional | Equipe pequena (3 a 6 devs), sem experiência prévia em sistemas distribuídos                                                                           | Mesma decisão: evita o custo de deploy e observabilidade distribuída sem necessidade comprovada |
| 1       | Manutenibilidade         | O case prevê evolução contínua do produto; cursos, conquistas e recompensas seguem evoluindo junto com o ranking, e não podem ficar acoplados entre si | Módulos de negócio isolados dentro do mesmo deploy                                              |

## Desafio 1 - Drivers e Evolução do Core

**Cenário:** o MVP cresce de 1.000 para até 100.000 usuários; o ranking cresce mais que as demais funcionalidades.

**Decisão:** evoluir o Core de Monólito em Camadas para **Monólito Modular**, organizando o código por módulo de negócio (cursos, ranking, conquistas, recompensas) dentro do mesmo deploy, em vez de manter a organização puramente técnica atual ou decompor em serviços já nesta rodada.

**Por quê, resumido:** não há ainda ASR comprovado por métrica real que justifique o custo operacional de um serviço separado (alternativa C), e manter a organização técnica atual (alternativa A) deixa ranking e cursos acoplados no mesmo namespace, sem nenhuma fronteira que proteja um módulo de mudanças no outro. O Monólito Modular resolve a manutenibilidade agora e deixa a extração futura barata, se a carga do ranking se confirmar na prática.

Detalhes completos (RF, RNF, ASRs, RPCs, alternativas, trade-offs, C4 Container e evidência/mini-lab): **[ADR-001](../adr/ADR-001-monolito-modular.md)**.

O mini-lab reorganizou o único módulo implementado hoje (cursos, US1) de "por camada técnica" para `cursos/api` + `cursos/internal`, confirmando que a modularização não exige reescrever regra nenhuma. `domain/` (TDD, 100% de cobertura) não precisou mudar de lugar.

## Avaliação Final (AF)

**Cenário:** plataforma de educação corporativa com avaliação de competências por ML, recomendação de cursos por RAG e agentes de IA, com RabbitMQ para mensageria e uma equipe reduzida.

**Decisão:** arquitetura **híbrida**. O Core continua monólito modular (mantém o ADR-001), e as capacidades de IA (ml-service em FastAPI, ai-service em Spring AI) ficam em serviços próprios, atrás de um API Gateway, com RabbitMQ para o treinamento de modelos. Decisão complementar: arquitetura **hexagonal** nos serviços com regra de negócio.

**Por quê, resumido:** separar a IA atende à evolução independente dela (RNF04) e ao treino com o ML fora do ar (RNF02), sem pagar o custo de quebrar o negócio em microsserviços que uma equipe de 3 teria que operar (RNF05).

Detalhes completos (requisitos, alternativas, ASRs, REST x RabbitMQ, hexagonal, trade-offs e validação): **[ADR-AF](../adr/ADR-AF-arquitetura-hibrida.md)**.
