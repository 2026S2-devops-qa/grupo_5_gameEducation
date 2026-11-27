// Aba "Assistente IA" (RF03 e RF04, Joao). Contrato das props no comentario do index.html.
// RF03: POST /api/ia/recomendacoes {"objetivo"} -> {"resposta","cursos":[{"titulo","motivo"}],"fontes":[...]}
// RF04: POST /api/ia/agentes/orientacao {"objetivo"} -> {"resumo","acoes":[AcaoAgente],"toolsUsadas":[...]}
//       POST /api/ia/agentes/acoes/{id}/autorizar | /negar -> AcaoAgente
//       GET  /api/ia/agentes/acoes -> [AcaoAgente] do usuario logado (admin ve todas)
window.ABAS.ia = {
    titulo: 'Assistente IA',
    ordem: 20,
    componente: {
        props: ['api', 'usuario'],
        data() {
            return {
                // RF03
                objetivo: '',
                carregando: false,
                erro: '',
                recomendacao: null,
                exemplos: [
                    'Quero crescer em DevOps',
                    'Trabalho com QA e quero automatizar testes',
                    'Quero me preparar para liderar um time'
                ],
                // RF04
                objetivoAgente: 'Montar meu plano do trimestre',
                orientando: false,
                erroAgente: '',
                plano: null,
                acoes: [],
                decidindo: null,
                msgAcao: ''
            };
        },
        computed: {
            podeOrientar() {
                return this.usuario && this.usuario.colaboradorId;
            }
        },
        mounted() {
            this.carregarAcoes();
        },
        methods: {
            usarExemplo(texto) {
                this.objetivo = texto;
                this.recomendar();
            },
            async recomendar() {
                if (!this.objetivo.trim() || this.carregando) return;
                this.carregando = true;
                this.erro = '';
                this.recomendacao = null;
                try {
                    this.recomendacao = await this.api('/api/ia/recomendacoes', {
                        method: 'POST',
                        body: { objetivo: this.objetivo.trim() }
                    });
                } catch (e) {
                    // 503 sem chave do LLM traz a mensagem do ai-service ("defina OPENAI_API_KEY no .env")
                    this.erro = e.message;
                } finally {
                    this.carregando = false;
                }
            },
            async orientar() {
                if (!this.objetivoAgente.trim() || this.orientando) return;
                this.orientando = true;
                this.erroAgente = '';
                this.plano = null;
                this.msgAcao = '';
                try {
                    this.plano = await this.api('/api/ia/agentes/orientacao', {
                        method: 'POST',
                        body: { objetivo: this.objetivoAgente.trim() }
                    });
                    await this.carregarAcoes();
                } catch (e) {
                    this.erroAgente = e.message;
                } finally {
                    this.orientando = false;
                }
            },
            async carregarAcoes() {
                try {
                    this.acoes = await this.api('/api/ia/agentes/acoes');
                } catch (e) {
                    this.acoes = [];
                }
            },
            async decidir(acao, decisao) {
                this.decidindo = acao.id;
                this.msgAcao = '';
                try {
                    const atualizada = await this.api('/api/ia/agentes/acoes/' + acao.id + '/' + decisao, { method: 'POST' });
                    this.msgAcao = (decisao === 'autorizar' ? 'Ação autorizada: ' : 'Ação negada: ') + (atualizada.resultado || '');
                    await this.carregarAcoes();
                } catch (e) {
                    this.msgAcao = e.message;
                } finally {
                    this.decidindo = null;
                }
            },
            rotuloStatus(status) {
                return { AGUARDANDO_AUTORIZACAO: 'Aguardando você', EXECUTADA: 'Executada', CANCELADA: 'Negada' }[status] || status;
            },
            corStatus(status) {
                if (status === 'EXECUTADA') return 'background:#dcfce7;color:#166534';
                if (status === 'CANCELADA') return 'background:#fee2e2;color:#991b1b';
                return 'background:#fef3c7;color:#92400e';
            },
            dataHora(iso) {
                return iso ? new Date(iso).toLocaleString('pt-BR') : '';
            }
        },
        template: `
            <div>
                <div class="grid">
                    <div class="card">
                        <h2>Recomendação de cursos (RAG)</h2>
                        <p class="vazio" style="margin-top:0">
                            Descreva seu objetivo. O assistente busca nos documentos institucionais
                            e recomenda só cursos que aparecem neles, citando as fontes.
                        </p>
                        <label>Seu objetivo</label>
                        <textarea v-model="objetivo" rows="3" maxlength="500"
                                  placeholder="Ex: quero crescer em DevOps"
                                  @keydown.enter.exact.prevent="recomendar"></textarea>
                        <button :disabled="carregando || !objetivo.trim()" @click="recomendar">
                            {{ carregando ? 'Consultando documentos...' : 'Recomendar cursos' }}
                        </button>
                        <p class="vazio" style="margin:14px 0 6px">Exemplos:</p>
                        <div style="display:flex;flex-wrap:wrap;gap:6px">
                            <button v-for="ex in exemplos" :key="ex" class="ghost"
                                    style="width:auto;padding:6px 10px;font-size:.8rem"
                                    :disabled="carregando" @click="usarExemplo(ex)">{{ ex }}</button>
                        </div>
                    </div>

                    <div class="card">
                        <h2>Resposta</h2>
                        <div v-if="erro" class="msg err">{{ erro }}</div>
                        <p v-else-if="carregando" class="vazio">Gerando a recomendação...</p>
                        <p v-else-if="!recomendacao" class="vazio">A recomendação aparece aqui.</p>
                        <template v-else>
                            <p style="margin-top:0;line-height:1.5">{{ recomendacao.resposta }}</p>

                            <h3 style="font-size:.9rem;margin:16px 0 8px">Cursos recomendados</h3>
                            <p v-if="!recomendacao.cursos.length" class="vazio">Nenhum curso encontrado nos documentos.</p>
                            <table v-else>
                                <thead><tr><th>Curso</th><th>Por quê</th></tr></thead>
                                <tbody>
                                    <tr v-for="c in recomendacao.cursos" :key="c.titulo">
                                        <td style="font-weight:600">{{ c.titulo }}</td>
                                        <td>{{ c.motivo }}</td>
                                    </tr>
                                </tbody>
                            </table>

                            <h3 style="font-size:.9rem;margin:16px 0 8px">Fontes consultadas</h3>
                            <p v-if="!recomendacao.fontes.length" class="vazio">Nenhuma.</p>
                            <div v-else style="display:flex;flex-wrap:wrap;gap:6px">
                                <span v-for="f in recomendacao.fontes" :key="f" class="tag">{{ f }}</span>
                            </div>
                        </template>
                    </div>
                </div>

                <div class="grid" style="margin-top:20px">
                    <div class="card">
                        <h2>Agente de orientação (com sua autorização)</h2>
                        <p class="vazio" style="margin-top:0">
                            O agente consulta o seu perfil, os cursos e os documentos e <strong>propõe</strong> ações.
                            Nada é executado sem você clicar em <strong>Autorizar</strong>.
                        </p>
                        <template v-if="podeOrientar">
                            <label>O que você quer planejar?</label>
                            <textarea v-model="objetivoAgente" rows="2" maxlength="500"
                                      @keydown.enter.exact.prevent="orientar"></textarea>
                            <button :disabled="orientando || !objetivoAgente.trim()" @click="orientar">
                                {{ orientando ? 'O agente está analisando...' : 'Pedir orientação' }}
                            </button>
                        </template>
                        <div v-else class="msg info">
                            O usuário admin não tem colaborador ligado: ele não pede orientação, mas vê e decide as ações de todos ao lado.
                        </div>
                        <div v-if="erroAgente" class="msg err">{{ erroAgente }}</div>
                        <template v-if="plano">
                            <h3 style="font-size:.9rem;margin:16px 0 8px">Plano proposto</h3>
                            <p style="margin-top:0;line-height:1.5;white-space:pre-line">{{ plano.resumo }}</p>
                            <p class="vazio" style="margin:10px 0 6px">Tools que o agente usou:</p>
                            <div style="display:flex;flex-wrap:wrap;gap:6px">
                                <span v-for="(t, i) in plano.toolsUsadas" :key="i" class="tag">{{ t }}</span>
                            </div>
                        </template>
                    </div>

                    <div class="card">
                        <h2>{{ usuario && usuario.admin ? 'Ações de todos os colaboradores' : 'Minhas ações' }}</h2>
                        <div v-if="msgAcao" class="msg info">{{ msgAcao }}</div>
                        <p v-if="!acoes.length" class="vazio">Nenhuma ação proposta ainda.</p>
                        <table v-else>
                            <thead><tr><th>Ação</th><th>Status</th><th></th></tr></thead>
                            <tbody>
                                <tr v-for="a in acoes" :key="a.id">
                                    <td>
                                        <div style="font-weight:600">#{{ a.id }} {{ a.descricao }}</div>
                                        <div class="vazio" style="font-size:.8rem">{{ a.motivo }}</div>
                                        <div v-if="usuario && usuario.admin" class="vazio" style="font-size:.75rem">de {{ a.solicitadoPor }}</div>
                                        <div v-if="a.resultado" style="font-size:.8rem;margin-top:4px">{{ a.resultado }}</div>
                                        <div v-if="a.autorizadoPor" class="vazio" style="font-size:.75rem">
                                            decidido por {{ a.autorizadoPor }} em {{ dataHora(a.autorizadoEm) }}
                                        </div>
                                    </td>
                                    <td><span class="tag" :style="corStatus(a.status)">{{ rotuloStatus(a.status) }}</span></td>
                                    <td style="white-space:nowrap">
                                        <template v-if="a.status === 'AGUARDANDO_AUTORIZACAO'">
                                            <button style="width:auto;padding:6px 10px;font-size:.8rem"
                                                    :disabled="decidindo === a.id" @click="decidir(a, 'autorizar')">Autorizar</button>
                                            <button class="ghost" style="width:auto;padding:6px 10px;font-size:.8rem;margin-left:4px"
                                                    :disabled="decidindo === a.id" @click="decidir(a, 'negar')">Negar</button>
                                        </template>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>`
    }
};
