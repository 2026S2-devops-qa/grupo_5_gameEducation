// Aba "Assistente IA" (RF03 e RF04, Joao). Contrato das props no comentario do index.html.
// RF03: POST /api/ia/recomendacoes {"objetivo"} -> {"resposta","cursos":[{"titulo","motivo"}],"fontes":[...]}
// RF04 (proximo PR): POST /api/ia/agentes/orientacao, POST /api/ia/agentes/acoes/{id}/autorizar | /negar,
// GET /api/ia/agentes/acoes.
window.ABAS.ia = {
    titulo: 'Assistente IA',
    ordem: 20,
    componente: {
        props: ['api', 'usuario'],
        data() {
            return {
                objetivo: '',
                carregando: false,
                erro: '',
                recomendacao: null,
                exemplos: [
                    'Quero crescer em DevOps',
                    'Trabalho com QA e quero automatizar testes',
                    'Quero me preparar para liderar um time'
                ]
            };
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
            }
        },
        template: `
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
            </div>`
    }
};
