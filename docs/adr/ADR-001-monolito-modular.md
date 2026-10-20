# ADR-001: Evoluir o Core para Monólito Modular

- **Desafio:** B1 - Desafio 1, Drivers e Evolução do Core
- **Data:** 2026-10-12

## Contexto

O MVP de gamificação (AC1) cresce, no cenário do exercício, de 1.000 para até 100.000 usuários. O ranking (RF01) é projetado para crescer de forma desproporcional em relação às demais funcionalidades, enquanto cursos, conquistas e recompensas (RF02) continuam evoluindo normalmente. É preciso decidir a forma macro do Core antes de construir o ranking, que ainda não existe no código.

Nota sobre o contexto real do projeto: o cenário do exercício descreve uma equipe de 6 desenvolvedores; a equipe real do grupo tem 3 (Bruno, Gabriel, João). Só a US1 (liberação de cursos) está implementada e testada; ranking (US2) e moedas, badges e Premium (US3) ainda são apenas BDD, sem código. Isso é tratado como reforço da decisão, não como contradição: não existe legado de ranking para migrar, então a escolha é sobre onde construir algo novo, não sobre como migrar algo que já existe.

O Core atual já é, estruturalmente, um Monólito em Camadas: um único deploy, organizado por camada técnica (`controller`, `service`, `repository`, `entity`, `domain`), sem nenhuma fronteira de negócio entre as funcionalidades.

## RF afetados

- RF01 Ranking (não implementado)
- RF02 Conquistas (parcialmente implementado: cursos liberados prontos; moedas, badges e Premium pendentes)
- Evolução contínua das funcionalidades existentes

## RNF analisados

Do conjunto sugerido pelo desafio (manutenibilidade, desempenho, escalabilidade, testabilidade, complexidade operacional), os cinco foram considerados antes de selecionar os que realmente decidem entre as alternativas.

## RNFs e seleção dos ASRs (máximo 3)

Nem todo RNF é automaticamente um ASR. Foram selecionados somente os atributos capazes de influenciar estrutura, implantação ou operação do Core.

| RNF                      | ASR?                  | Justificativa arquitetural                                                                                                                                                                             |
| ------------------------ | --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Manutenibilidade         | SIM                   | O case prevê evolução contínua do produto, e cursos, conquistas e recompensas seguem evoluindo junto com o ranking; sem fronteira interna entre módulos de negócio, a evolução de um arrisca os outros |
| Escalabilidade           | SIM                   | Se o ranking precisa suportar carga muito maior que o resto, a estrutura precisa permitir escalar só essa capacidade, não o sistema inteiro                                                            |
| Complexidade operacional | SIM                   | Cada processo, banco ou pipeline adicional é custo recorrente que uma equipe pequena (3 a 6 devs) precisa absorver; é o contrapeso que evita decompor por impulso                                      |
| Desempenho               | NÃO, neste momento    | É consequência direta de escalabilidade: o problema não é "processar devagar", é "uma parte específica recebe carga muito maior que o resto"; não há medição de latência/throughput que prove gargalo  |
| Testabilidade            | NÃO como driver macro | O que está em jogo nesta decisão é a forma macro do Core (manter, modularizar ou decompor), não a forma como cada parte é testada internamente                                                         |

## RPCs - restrições e premissas

- Core permanece Java/Spring Boot (não é uma opção em aberto)
- Equipe pequena: 6 desenvolvedores no cenário do exercício, 3 na equipe real
- Banco relacional e legado da AC1 (PostgreSQL em produção/Docker, H2 em dev/teste); a US1 já está implementada e testada, então a solução deve evoluir incrementalmente, sem reescrever o que já funciona
- Não existe legado de ranking a migrar, é funcionalidade nova

## Alternativas consideradas

| Alternativa                                              | Benefício                                                                                                                                                                                                                                 | Custo/risco                                                                                                                                                                                        | ASR que justificaria escolhê-la                                                                                       |
| -------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------- |
| (A) Monólito em Camadas (manter como está)               | Zero esforço de migração, time já conhece a estrutura                                                                                                                                                                                     | Ranking e cursos no mesmo namespace técnico; mudança de regra de um arrisca o outro; nada prepara separação futura                                                                                 | Só se a carga desigual do ranking nunca se confirmar na prática                                                       |
| **(B) Monólito Modular** - escolhida                     | Isola ranking, cursos e moedas em módulos de negócio dentro do mesmo deploy; escala o processo inteiro via réplicas sem reescrever nada; prepara extração futura de baixo custo; mantém o custo operacional de hoje (um deploy, um banco) | Exige disciplina para não vazar dependência entre módulos; não resolve sozinha uma assimetria de carga muito grande                                                                                | Manutenibilidade como prioridade alta, com escalabilidade ainda não comprovada ao ponto de exigir deploy independente |
| (C) Decomposição seletiva (ranking como serviço próprio) | Escala o ranking sozinho, sob demanda, sem carregar cursos e moedas junto; deploy independente                                                                                                                                            | Observabilidade, comunicação entre serviços, consistência de dados entre Core e ranking, e operação de mais um processo, tudo isso para uma equipe sem experiência prévia em sistemas distribuídos | Escalabilidade comprovada por métrica real, mais maturidade operacional da equipe para sustentar                      |

## Decisão

Evoluir o Core para **Monólito Modular (B)**: reorganizar o código por módulo de negócio (cursos, ranking, conquistas, recompensas), cada um com fronteira interna clara, dentro do mesmo processo e do mesmo deploy.

Não (A), porque manter a organização puramente técnica atual mistura cursos e o futuro ranking no mesmo namespace, sem nenhuma fronteira que proteja um módulo de mudanças no outro. Não (C) ainda, porque não há ASR comprovado por métrica real que justifique pagar o custo operacional de um serviço separado com uma equipe deste tamanho, que nunca operou nada distribuído.

## Consequências / trade-offs

- Ganha manutenibilidade: regras de ranking, cursos e recompensas evoluem com menos risco de interferência mútua.
- Prepara extração futura de baixo custo: se o ranking precisar mesmo escalar sozinho, extrair um módulo já isolado é muito mais barato que desembaraçar um monólito em camadas.
- Não resolve, por si só, uma assimetria de carga extrema. Se isso se confirmar com métrica real de produção, a decomposição seletiva do ranking (C) passa a ser o próximo passo natural, não um recomeço.
- Trade-off aceito conscientemente: abrimos mão do ganho imediato de escalabilidade independente (que só (C) daria) em troca de menor complexidade operacional e menor risco para uma equipe pequena.

## Gatilho de revisão

Reavaliar a extração do módulo de ranking para um serviço independente (alternativa C) quando houver métrica real de produção mostrando gargalo, necessidade de escala própria ou frequência de deploy que justifique o custo operacional adicional. Até lá, a decisão permanece Monólito Modular.

## C4 Container

Pendente.

## Evidência / mini-lab

Pendente.
