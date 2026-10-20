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

| #   | Desafio                     | Status                                       | ADR                                           |
| --- | --------------------------- | -------------------------------------------- | --------------------------------------------- |
| 1   | Drivers e Evolução do Core  | Decisão registrada, C4 e evidência pendentes | [ADR-001](../adr/ADR-001-monolito-modular.md) |
| 2   | Organização Interna do Core | a fazer                                      | -                                             |
| 3   | Serviço Python e IA         | a fazer                                      | -                                             |
| 4   | Comunicação e Eventos       | a fazer                                      | -                                             |
| 5   | IoT e MQTT                  | a fazer                                      | -                                             |
| 6   | CI/CD e Observabilidade     | a fazer                                      | -                                             |

## Matriz cumulativa de ASRs

| Desafio | ASR                      | Por que virou ASR                                                                                    | Decisão resultante                                                                              |
| ------- | ------------------------ | ---------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------- |
| 1       | Escalabilidade           | Ranking projetado para crescer desproporcionalmente em relação às demais funcionalidades             | Monólito Modular, isolando o módulo de ranking para uma extração futura de baixo custo          |
| 1       | Complexidade operacional | Equipe pequena (3 a 6 devs), sem experiência prévia em sistemas distribuídos                         | Mesma decisão: evita o custo de deploy e observabilidade distribuída sem necessidade comprovada |
| 1       | Manutenibilidade         | O case prevê evolução contínua do produto; cursos, conquistas e recompensas seguem evoluindo junto com o ranking, e não podem ficar acoplados entre si | Módulos de negócio isolados dentro do mesmo deploy                                              |

## Desafio 1 - Drivers e Evolução do Core

**Cenário:** o MVP cresce de 1.000 para até 100.000 usuários; o ranking cresce mais que as demais funcionalidades.

**Decisão:** evoluir o Core de Monólito em Camadas para **Monólito Modular**, organizando o código por módulo de negócio (cursos, ranking, conquistas, recompensas) dentro do mesmo deploy, em vez de manter a organização puramente técnica atual ou decompor em serviços já nesta rodada.

**Por quê, resumido:** não há ainda ASR comprovado por métrica real que justifique o custo operacional de um serviço separado (alternativa C), e manter a organização técnica atual (alternativa A) deixa ranking e cursos acoplados no mesmo namespace, sem nenhuma fronteira que proteja um módulo de mudanças no outro. O Monólito Modular resolve a manutenibilidade agora e deixa a extração futura barata, se a carga do ranking se confirmar na prática.

Detalhes completos (RF, RNF, ASRs, RPCs, alternativas, trade-offs): **[ADR-001](../adr/ADR-001-monolito-modular.md)**.

Pendente para fechar o desafio: C4 Container e evidência/mini-lab.
